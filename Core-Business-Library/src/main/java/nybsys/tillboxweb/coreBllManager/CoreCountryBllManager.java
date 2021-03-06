/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 23-Apr-18
 * Time: 12:51 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.coreBllManager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.coreEntities.Country;
import nybsys.tillboxweb.coreModels.CountryModel;
import org.springframework.stereotype.Service;

@Service
public class CoreCountryBllManager extends BaseBll<Country> {
    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Country.class);
        Core.runTimeModelType.set(CountryModel.class);
    }
}