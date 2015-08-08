## Country ##

View all Countries:
```
import com.google.code.magja.model.country.Country;
import com.google.code.magja.service.RemoteServiceFactory;
import com.google.code.magja.service.country.CountryRemoteService;
import com.google.code.magja.soap.MagentoSoapClient;
import java.util.List;

public class MagentoCountryViewer {
    public static void main(String args[]) {
        try {
            // get soap instance
            MagentoSoapClient magentoSoapClient = MagentoSoapClient.getInstance();

            // get country service
            CountryRemoteService countryRemoteService = new RemoteServiceFactory().getCountryRemoteService();
            countryRemoteService.setSoapClient(magentoSoapClient);

            // get list of all countries
            List<Country> countries = countryRemoteService.list();

            // display all countries
            for (Country country : countries) {
                System.out.println(country);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
```