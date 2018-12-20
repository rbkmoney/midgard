import com.rbkmoney.midgard.adapter.mts.data.ClearingData;
import com.rbkmoney.midgard.adapter.mts.data.MerchantData;
import com.rbkmoney.midgard.adapter.mts.data.TransactionData;
import com.rbkmoney.midgard.adapter.mts.utils.XmlUtil;
import data.ClearingTransactionsTestData;
import data.MerchantsTestData;
import org.junit.Test;

import java.util.List;

public class XmlUtilTest {

    @Test
    public void createXml() {
        String xml = XmlUtil.createXml(getClearingData());

        //TODO: Это чисто для отладки. Полноценные тесты будут написаны позднее
        System.out.println("XML: " + xml);
    }

    private ClearingData getClearingData() {
        List<MerchantData> merchants = MerchantsTestData.getMerchants();
        List<TransactionData> transactions = ClearingTransactionsTestData.getClearingTransactions();
        ClearingData clearingData = new ClearingData();
        clearingData.setMerchants(merchants);
        clearingData.setTransactions(transactions);
        return clearingData;
    }

}
