package rails.game.action;

import java.io.IOException;
import java.io.ObjectInputStream;

import com.google.common.base.Objects;

import net.sf.rails.game.*;
import net.sf.rails.game.model.PortfolioModel;
import net.sf.rails.game.model.PortfolioOwner;
import net.sf.rails.util.RailsObjects;

/**
 * 
 * Rails 2.0: Updated equals and toString methods
 */
public class BuyCertificate extends PossibleAction {

    // Server-side settings

    /* Some obsolete properties, which are only retained for backwards compatibility
     * (i.e. to remain able to load older saved files).
     * The certificate was in fact only used to find the below replacement
     * attributes. It was NOT actually used to select the bought certificate!
     */
    transient protected PublicCertificate certificate = null;
    protected String certUniqueId = null;

    /* Replacement for the above.*/
    transient protected PublicCompany company;
    protected String companyName;
    protected int sharePerCert; // Share % per buyable certificate.

    // FIXME: We have to recreate the portfolio name
    transient protected PortfolioModel from;
    protected String fromName; // Old: portfolio name. New: portfolio unique name.
    protected int price;
    protected int maximumNumber;

    // Client-side settings
    protected int numberBought = 0;

    public static final long serialVersionUID = 1L;

    public BuyCertificate(PublicCompany company, int sharePerCert,
            PortfolioOwner from,
            int price, int maximumNumber) {
        this.company = company;
        this.sharePerCert = sharePerCert;
        this.from = from.getPortfolioModel();
        // FIXME: From used to be a Portfolio(model) with unique name to identify
        // this.fromName = from.getUniqueName();
        this.price = price;
        this.maximumNumber = maximumNumber;

        companyName = company.getId();
    }

    /** Buy a certificate from some owner at a given price */
    public BuyCertificate(PublicCompany company, int sharePerCert,
            PortfolioOwner from,
            int price) {
        this(company, sharePerCert, from, price, 1);
    }

    /** Required for deserialization */
    public BuyCertificate() {}

    public PortfolioModel getFromPortfolio() {
        return from;
    }

    /**
     * @return Returns the maximumNumber.
     */
    public int getMaximumNumber() {
        return maximumNumber;
    }

    /**
     * @return Returns the price.
     */
    public int getPrice() {
        return price;
    }

    public PublicCompany getCompany() {
        return company;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getSharePerCertificate() {
        return sharePerCert;
    }

    public int getSharesPerCertificate() {
        return sharePerCert / company.getShareUnit();
    }

    public int getNumberBought() {
        return numberBought;
    }

    public void setNumberBought(int numberBought) {
        this.numberBought = numberBought;
    }

    @Override
    public boolean equalsAsOption(PossibleAction pa) {
        // identity always true
        if (pa == this) return true;
        //  super checks both class identity and super class attributes
        if (!super.equalsAsOption(pa)) return false; 

        // check further attributes
        BuyCertificate action = (BuyCertificate)pa; 
        return 
                // TODO: This is commented out as the certificate is not required anymore
                // Objects.equal(this.certificate, action.certificate)
                Objects.equal(this.company, action.company)
// FIXME: This is commented out to avoid invalidate StartCompany_1880
//                && Objects.equal(this.sharePerCert, action.sharePerCert)
                && Objects.equal(this.from, action.from)
// FIXME: This is commented out to avoid invalidate StartCompany, see there
//                && Objects.equal(this.price, action.price)
                && Objects.equal(this.maximumNumber, action.maximumNumber)
        ;
    }

    @Override
    public boolean equalsAsAction(PossibleAction pa) {
        // first check if equal as option
        if (!this.equalsAsOption(pa)) return false;
        
        // check further attributes
        BuyCertificate action = (BuyCertificate)pa; 
        return Objects.equal(this.numberBought, action.numberBought);
    }

    @Override
    public String toString() {
        return super.toString() + 
                RailsObjects.stringHelper(this)
                    .addToString("certificate", certificate)
                    .addToString("company", company)
                    .addToString("sharePerCert", sharePerCert)
                    .addToString("from", from)
                    .addToString("price", price)
                    .addToString("maximumNumber", maximumNumber)
                    .addToStringOnlyActed("numberBought", numberBought)
                    .toString()
        ;
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {

        //in.defaultReadObject();
        // Custom reading for backwards compatibility
        ObjectInputStream.GetField fields = in.readFields();

        certUniqueId = (String) fields.get("certUniqueId", null);
        companyName = (String) fields.get("companyName", null);
        fromName = (String) fields.get("fromName", fromName);
        price = fields.get("price", price);
        maximumNumber = fields.get("maximumNumber", maximumNumber);
        sharePerCert = fields.get("sharePerCert", -1);

        numberBought = fields.get("numberBought", numberBought);

        RailsRoot root = RailsRoot.getInstance();

        /* Check for aliases (old company names) */
        CompanyManager companyManager = root.getCompanyManager();
        companyName = companyManager.checkAlias (companyName);

        if (certUniqueId != null) {
            // Old style
            certUniqueId = companyManager.checkAliasInCertId(certUniqueId);
            certificate = PublicCertificate.getByUniqueId(certUniqueId);
            // TODO: This function needs a compatible replacement 
            from = getGameManager().getPortfolioByName(fromName);
            company = certificate.getCompany();
            companyName = company.getId();
            sharePerCert = certificate.getShare();
        } else if (companyName != null) {
            // New style (since Rails.1.3.1)
            company = root.getCompanyManager().getPublicCompany(companyName);
            // TODO: This function needs a compatible replacement 
            from = getGameManager().getPortfolioByUniqueName(fromName);
            // We don't need the certificate anymore.
        }


    }
}
