package ru.hh.vsplitter.split;

import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XmlErrorHandler implements ErrorHandler {
  private final Logger logger;

  public XmlErrorHandler(Logger logger) {
    this.logger = logger;
  }

  private String getParseExceptionInfo(SAXParseException spe) {
    String systemId = spe.getSystemId();

    if (systemId == null) {
      systemId = "null";
    }

    return "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
  }

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    logger.warn(getParseExceptionInfo(exception));
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    throw new SAXException(getParseExceptionInfo(exception), exception);
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    throw new SAXException(getParseExceptionInfo(exception), exception);
  }
}
