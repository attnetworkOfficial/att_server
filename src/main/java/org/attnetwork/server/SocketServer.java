package org.attnetwork.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import org.attnetwork.exception.AException;
import org.attnetwork.server.component.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SocketServer implements Closeable {
  private final Logger log = LoggerFactory.getLogger(SocketServer.class);

  private static final long READ_INTERVAL = 10; //ms
  private static final int MESSAGE_BUFFER_SIZE = 1024; //bytes
  private final Selector selector;
  private final ByteBuffer buffer;
  private final MessageBufferPool messageBufferPool;
  private final ExecutorService executorService;
  private final MessageService messageService;
  private final ServerSocketChannel serverSocketChannel;

  @Autowired
  private SocketServer(@Value("${server.port}") int port, MessageService messageService) throws IOException {
    this.messageService = messageService;
    this.selector = Selector.open();
    this.buffer = ByteBuffer.allocateDirect(1024 * 1024);
    this.messageBufferPool = new MessageBufferPool(MESSAGE_BUFFER_SIZE, 10);
    this.executorService = Executors.newFixedThreadPool(2);
    this.serverSocketChannel = port < 0 ? null : ServerSocketChannel.open().bind(new InetSocketAddress(++port));
    log.info("Nio Socket server initialized with port: {}", port);
    if (port > 0) {
      start();
    }
  }

  private void start() throws IOException {
    SocketChannel channel = this.serverSocketChannel.accept();
    channel.configureBlocking(false).register(selector, SelectionKey.OP_READ);
    new Thread(() -> {
      try {
        read();
        Thread.sleep(READ_INTERVAL);
      } catch (Exception e) {
        throw AException.wrap(e);
      }
    }).start();
  }


  private void read() throws IOException {
    int readReady = selector.selectNow();
    if (readReady > 0) {
      Set<SelectionKey> selectedKeys = selector.selectedKeys();
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        keyIterator.remove();
        read(key);
      }
      selectedKeys.clear();
    }
  }

  private void read(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    int bytesRead;
    while (true) {
      if ((bytesRead = channel.read(buffer)) <= 0) {
        break;
      }
    }
    buffer.flip();
    MessageBuffer mb = (MessageBuffer) key.attachment();
    if (bytesRead == -1) {
      // connection closed
      close(key);
    } else {
      if (mb == null) {
        mb = new MessageBuffer();
        key.attach(mb);
      }
      mb.loadBuffer(buffer);
      executorService.submit(() -> {
        try {
          processMessage(key);
        } catch (IOException e) {
          log.error("", e);
        }
      });
    }
    buffer.clear();
  }

  private void close(SelectionKey key) throws IOException {
    key.attach(null);
    key.cancel();
    key.channel().close();
    log.debug("connection close");
  }

  private void processMessage(SelectionKey key) throws IOException {
    MessageBuffer mb = (MessageBuffer) key.attachment();
    InputStream is = mb.readBuffer();
    if (is != null) {
      ByteBuffer cache = messageBufferPool.poll();
      cache.clear();
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        messageService.process(is, os);
        byte[] result = os.toByteArray();
        SocketChannel channel = (SocketChannel) key.channel();
        int start = 0;
        while (start < result.length) {
          int len = Math.min(result.length - start, MESSAGE_BUFFER_SIZE);
          cache.put(result, start, len);
          cache.flip();
          channel.write(cache);
          cache.clear();
          start += len;
        }
      } catch (Exception e) {
        log.error("processMessage error", e);
        close(key);
      } finally {
        messageBufferPool.add(cache);
        mb.afterRead();
      }
    }
  }


  @Override
  @PreDestroy
  public synchronized void close() throws IOException {
    if (serverSocketChannel != null) {
      if (serverSocketChannel.isOpen()) {
        serverSocketChannel.close();
      }
    }
    log.info("Nio Socket server destroyed");
  }
}

class MessageBuffer {
  private static final Logger log = LoggerFactory.getLogger(MessageBuffer.class);

  private byte[] cache;
  private int cacheLength;
  private int dataLength;

  MessageBuffer() {
    cache = new byte[256];
    cacheLength = 0;
  }

  void loadBuffer(ByteBuffer buffer) {
    int remaining = buffer.remaining();
    expandCache(remaining);
    buffer.get(cache, cacheLength, remaining);
    cacheLength += remaining;
  }

  InputStream readBuffer() {
    dataLength = 0;
    int i = 0;
    while (i < cacheLength) {
      int oneByte = cache[i];
      dataLength = dataLength << 7;
      dataLength += oneByte & 0x7F;
      if ((oneByte >>> 7) == 0) {
        break;
      }
      ++i;
    }
    if (dataLength < cacheLength && i < cacheLength) {
      // data ready
      return new ByteArrayInputStream(cache, 0, dataLength);
    } else {
      return null;
    }
  }

  void afterRead() {
    // move unread data to the head
    try {
      System.arraycopy(cache, dataLength, cache, 0, cacheLength - dataLength);
      cacheLength -= dataLength;
    } catch (Exception e) {
      log.info("afterRead error", e);
      cacheLength = 0;
    }
  }


  private void expandCache(int remaining) {
    int cl = cache.length;
    while (cl < cacheLength + remaining) {
      cl <<= 1;
      if (cl < 0) {
        cl = cacheLength + remaining;
        break;
      }
    }
    byte[] newCache = new byte[cl];
    System.arraycopy(cache, 0, newCache, 0, cacheLength);
    cache = newCache;
  }
}

class MessageBufferPool {
  private final ConcurrentLinkedDeque<ByteBuffer> pool;
  private final int bufferSize;

  MessageBufferPool(int bufferSize, int initAmount) {
    this.bufferSize = bufferSize;
    pool = new ConcurrentLinkedDeque<>();
    for (int i = 0; i < initAmount; i++) {
      add(newCache());
    }
  }

  ByteBuffer poll() {
    ByteBuffer cache = pool.poll();
    return cache == null ? newCache() : cache;
  }

  private ByteBuffer newCache() {
    return ByteBuffer.allocateDirect(bufferSize);
  }

  void add(ByteBuffer cache) {
    pool.add(cache);
  }

  int size() {
    return pool.size();
  }
}