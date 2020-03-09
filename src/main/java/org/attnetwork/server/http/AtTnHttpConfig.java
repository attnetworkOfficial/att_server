package org.attnetwork.server.http;

import java.io.IOException;
import java.util.List;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.server.component.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
class AtTnHttpConfig extends WebMvcConfigurationSupport {
  private final HttpService httpService;

  @Autowired
  public AtTnHttpConfig(HttpService httpService) {
    this.httpService = httpService;
  }

  @Override
  protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new AbstractHttpMessageConverter<AbstractSeqLanObject>(MediaType.ALL) {
      protected boolean supports(Class<?> clazz) {
        return true;
//        return AbstractSeqLanObject.class.isAssignableFrom(clazz);
      }

      protected AbstractSeqLanObject readInternal(Class<? extends AbstractSeqLanObject> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return httpService.readInternal(clazz, inputMessage);
      }

      protected void writeInternal(AbstractSeqLanObject o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        o.write(outputMessage.getBody());
      }
    });
    super.configureMessageConverters(converters);
  }

}