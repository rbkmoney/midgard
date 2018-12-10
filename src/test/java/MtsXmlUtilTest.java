import com.rbkmoney.midgard.clearing.data.ClearingData;
import com.rbkmoney.midgard.mts_clearing_adapter.utils.MtsXmlUtil;
import data.ClearingTransactionsTestData;
import data.MerchantsTestData;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.Merchant;
import org.junit.Test;

import java.util.List;

public class MtsXmlUtilTest {

    @Test
    public void createXml() {
        String xml = MtsXmlUtil.createXml(getClearingData());

        //TODO: Это чисто для отладки. Полноценные тесты будут написаны позднее
        System.out.println("XML: " + xml);
    }

    private ClearingData getClearingData() {
        List<Merchant> merchants = MerchantsTestData.getMerchants();
        List<ClearingTransaction> transactions = ClearingTransactionsTestData.getClearingTransactions();
        return new ClearingData(transactions, merchants);
    }

}
