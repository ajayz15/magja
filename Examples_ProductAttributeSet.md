## ProductAttributeSet ##

View all Product Attribute Sets:
```
MagentoSoapClient magentoSoapClient = MagentoSoapClient.getInstance();
ProductAttributeRemoteService service = new RemoteServiceFactory().getProductAttributeRemoteService();
service.setSoapClient(magentoSoapClient);

List<ProductAttributeSet> listProductAttributeSet = service.listAllProductAttributeSet();
for (ProductAttributeSet productAttributeSet : listProductAttributeSet) {
   System.out.println(productAttributeSet.getId() + ":" + productAttributeSet.getName());
}
```