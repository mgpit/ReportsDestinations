/* DO NOT EDIT THIS FILE - This file was generated by     */
/* Oracle JAXB Class Generator Implementation.            */
/* Any Modification to this file will be lost upon        */
/* recompilation of source schema                         */ 

package de.mgpit.oracle.reports.plugin.destination.cdm.schema;


public class CdmdocTypeImpl extends oracle.xml.jaxb.JaxbNode implements de.mgpit.oracle.reports.plugin.destination.cdm.schema.CdmdocType
{
   public CdmdocTypeImpl(oracle.xml.parser.v2.XMLDocument ownerDoc)
   {
      super("cdmdoc", "", ownerDoc); 
   }

   public CdmdocTypeImpl(java.lang.String name, java.lang.String namespace, oracle.xml.parser.v2.XMLDocument ownerDoc)
   {
      super(name, namespace, ownerDoc);
   }

   public CdmdocTypeImpl(oracle.xml.parser.v2.XMLElement node)
   {
      super(node);
   }

   public void setUnifier(java.lang.String value)
   {
      java.lang.String lexval = oracle.xml.jaxb.JaxbDatatypeConverter.printAnySimpleType(value);
      super.setJaxbAttr("unifier", "", lexval);
   }

   public java.lang.String getUnifier()
   {
      java.lang.String lexval = super.getJaxbAttr("unifier", "");
      return oracle.xml.jaxb.JaxbDatatypeConverter.parseAnySimpleType(lexval);
   }

   public void setContent(de.mgpit.oracle.reports.plugin.destination.cdm.schema.ContentType value)
   {
      super.setElement("content", "", (oracle.xml.jaxb.JaxbNode)value, 0);
   }

   public de.mgpit.oracle.reports.plugin.destination.cdm.schema.ContentType getContent()
   {
      de.mgpit.oracle.reports.plugin.destination.cdm.schema.ContentTypeImpl obj = new de.mgpit.oracle.reports.plugin.destination.cdm.schema.ContentTypeImpl(getOwnerDocument());
      return (de.mgpit.oracle.reports.plugin.destination.cdm.schema.ContentType) super.getElement("content", "", (oracle.xml.jaxb.JaxbNode)obj, 0);
   }

   public void populateNodeArray(oracle.xml.parser.v2.XMLNode node)
   {
      java.lang.String name, namespace;
      oracle.xml.parser.v2.XMLNode n = (oracle.xml.parser.v2.XMLNode)node.getFirstChild();

      int i = 0;
      while (n != null)
      {
         name = n.getLocalName(); 
         namespace = n.getNamespaceURI();

         if (name == null)
         {
            n = (oracle.xml.parser.v2.XMLNode)n.getNextSibling();
            i++;
            continue;
         }

         if (namespace == null)
            namespace = "";

         if (name != null)
         {
            if (name.equals("content") && namespace.equals(""))
            super.setNodeArrayValue(0, n);
         }

         n = (oracle.xml.parser.v2.XMLNode)n.getNextSibling();
         i++;
      }

      super.populateNodeArray(node);
   }

   static final Object[] _CdmdocType = 
   {"content"};

   protected Object[] getSchemaObject()
   {
      return _CdmdocType;
   }

}
