package org.attnetwork.server.component;

import java.io.IOException;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.springframework.http.HttpInputMessage;

public interface HttpService {
  AbstractSeqLanObject readInternal(Class<? extends AbstractSeqLanObject> type, HttpInputMessage inputMessage) throws IOException;
}
