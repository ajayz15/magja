## ProductAttribute ##

View all Product Attributes from a Set:
```
MagentoSoapClient magentoSoapClient = MagentoSoapClient.getInstance();
ProductAttributeRemoteService service = new RemoteServiceFactory().getProductAttributeRemoteService();
service.setSoapClient(magentoSoapClient);

ProductAttributeSet productAttributeSet = new ProductAttributeSet();
productAttributeSet.setId(4); // 4 = "Default" ProductAttributeSet
List<ProductAttribute> listProductAttribute = service.listByAttributeSet(productAttributeSet);
for (ProductAttribute productAttribute : listProductAttribute) {
   System.out.println(productAttribute.getCode());
}
```