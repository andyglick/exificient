package com.siemens.ct.exi.util;

import com.siemens.ct.exi.EXIBodyDecoder;
import com.siemens.ct.exi.context.QNameContext;
import com.siemens.ct.exi.core.container.DocType;
import com.siemens.ct.exi.core.container.ProcessingInstruction;
import com.siemens.ct.exi.exceptions.EXIException;
import com.siemens.ct.exi.grammars.event.EventType;
import com.siemens.ct.exi.values.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author glick
 */
public class AbstractXmlParser
{
  public static class AttributeContainer {
    public final QNameContext qname;
    public final Value value;
    public final String prefix;

    public AttributeContainer(QNameContext qname, Value value, String prefix) {
      this.qname = qname;
      this.value = value;
      this.prefix = prefix;
    }
  }

  protected DocType docType;
  protected EXIBodyDecoder decoder;
  /* current event */
  protected EventType eventType;
  protected QNameContext element;
  protected List<AttributeContainer> attributes;
  protected Value characters;
  protected char[] entityReference;
  protected char[] comment;
  protected ProcessingInstruction processingInstruction;
  protected String endElementPrefix;

  public AbstractXmlParser()
  {
    this.attributes = new ArrayList<AttributeContainer>();
  }

  protected String getDocTypeString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE ");
    sb.append(docType.name);

    if (docType.publicID.length > 0) {
      sb.append(" PUBLIC ");
      sb.append('\"');
      sb.append(docType.publicID);
      sb.append('\"');
    }
    if (docType.systemID.length > 0) {
      if (docType.publicID.length == 0) {
        sb.append(" SYSTEM ");
      } else {
        sb.append(' ');
      }
      sb.append('\"');
      sb.append(docType.systemID);
      sb.append('\"');
    }
    if (docType.text.length > 0) {
      sb.append(' ');
      sb.append('[');
      sb.append(docType.text);
      sb.append(']');
    }
    sb.append('>');

    return sb.toString();
  }

  // without further attribute handling
  protected EventType decodeEvent(EventType nextEventType)
      throws EXIException, IOException
  {

    endElementPrefix = null;

    switch (nextEventType) {
    /* DOCUMENT */
    case START_DOCUMENT:
      decoder.decodeStartDocument();
      break;
    case END_DOCUMENT:
      decoder.decodeEndDocument();
      break;
    /* ATTRIBUTES */
    case ATTRIBUTE_XSI_NIL:
      attributes.add(new AttributeContainer(decoder
          .decodeAttributeXsiNil(), decoder.getAttributeValue(),
          decoder.getAttributePrefix()));
      break;
    case ATTRIBUTE_XSI_TYPE:
      attributes.add(new AttributeContainer(decoder
          .decodeAttributeXsiType(), decoder.getAttributeValue(),
          decoder.getAttributePrefix()));
      break;
    case ATTRIBUTE:
    case ATTRIBUTE_NS:
    case ATTRIBUTE_GENERIC:
    case ATTRIBUTE_GENERIC_UNDECLARED:
    case ATTRIBUTE_INVALID_VALUE:
    case ATTRIBUTE_ANY_INVALID_VALUE:
      attributes.add(new AttributeContainer(decoder.decodeAttribute(),
          decoder.getAttributeValue(), decoder.getAttributePrefix()));
      break;
    /* NAMESPACE DECLARATION */
    case NAMESPACE_DECLARATION:
      // Note: Prefix declaration etc. is done internally
      decoder.decodeNamespaceDeclaration();
      break;
    /* SELF_CONTAINED */
    case SELF_CONTAINED:
      decoder.decodeStartSelfContainedFragment();
      break;
    /* ELEMENT CONTENT EVENTS */
    /* START ELEMENT */
    case START_ELEMENT:
    case START_ELEMENT_NS:
    case START_ELEMENT_GENERIC:
    case START_ELEMENT_GENERIC_UNDECLARED:
      element = decoder.decodeStartElement();
      break;
    /* END ELEMENT */
    case END_ELEMENT:
    case END_ELEMENT_UNDECLARED:
//			@SuppressWarnings("unused")
//			List<NamespaceDeclaration> eePrefixes = decoder.getDeclaredPrefixDeclarations();
//			if (namespacePrefixes) {
//				// eeQNameAsString = decoder.getElementQNameAsString();
//			}
      endElementPrefix = decoder.getElementPrefix();
      element = decoder.decodeEndElement();
      break;
    /* CHARACTERS */
    case CHARACTERS:
    case CHARACTERS_GENERIC:
    case CHARACTERS_GENERIC_UNDECLARED:
      characters = decoder.decodeCharacters();
      break;
    /* MISC */
    case DOC_TYPE:
      docType = decoder.decodeDocType();
      break;
    case ENTITY_REFERENCE:
      entityReference = decoder.decodeEntityReference();
      break;
    case COMMENT:
      comment = decoder.decodeComment();
      break;
    case PROCESSING_INSTRUCTION:
      processingInstruction = decoder.decodeProcessingInstruction();
      break;
    default:
      throw new RuntimeException("Unexpected EXI Event '" + eventType
          + "' ");
    }

    return nextEventType;
  }
}
