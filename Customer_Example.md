## Customer ##

View all Customer:
```
import com.google.code.magja.model.customer.Customer;
import com.google.code.magja.service.RemoteServiceFactory;
import com.google.code.magja.service.customer.CustomerRemoteService;
import com.google.code.magja.soap.SoapConfig;

public class MagjaCustomerExample {

    public static void main(String[] args) {
        try {
            SoapConfig soapConfig = new SoapConfig("USER", "PASS", "http://HOST/index.php/api/soap/");
            CustomerRemoteService customerRemoteService = RemoteServiceFactory.getCustomerRemoteService(soapConfig);

            for (Customer customer : customerRemoteService.list()) {
                System.out.println(customer.toString());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
```