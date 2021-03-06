package cn.nit.stock;

import cn.nit.stock.model.StockLimit;
import cn.nit.stock.model.StockName;
import cn.nit.stock.model.TradeDay;
import com.mongodb.MongoClient;
import org.apache.commons.io.FileUtils;
import org.mongodb.morphia.Datastore;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gengke on 2014/10/23.
 */
public class SuccessiveLimit {

    private static Datastore ds;

    private static MongoClient mongoClient;

    private static MongoOperations mongoOps;

    public static void main( String[] args ) throws Exception {
        ds = ConnUtils.getDatastore();
        mongoClient = ConnUtils.getMongo();
        mongoOps = new MongoTemplate(mongoClient, "stock");

       List<String> limit = new ArrayList<String>();

        for (StockName stockName : ds.find(StockName.class).asList()) {
            System.err.println(stockName);

            String stockcode = stockName.getCode();

            List<TradeDay> list = mongoOps.find(new Query(Criteria.where("closePrice").gt(0)).with(new Sort(Sort.Direction.DESC, "tradeDate")).limit(3),TradeDay.class, stockName.getCode());

            if (list.size() < 3) continue;

                TradeDay day1 = list.get(0);
                TradeDay day2 = list.get(1);
                TradeDay day3 = list.get(2);

                if (!day1.isTodayLimit()) continue;

                if (day1.isOpenLimit() && day2.isOpenLimit() && day3.isOpenLimit()) {
                    String str = "0" + stockcode;

                    if (stockcode.startsWith("6"))
                        str = "1" + stockcode;

                    if (!limit.contains(str)) limit.add(str);
                }


        }

        if (limit.size() == 0) return;


        File zxgFile = new File("C:\\new_gxzq_v6\\T0002\\blocknew\\ZXG.blk");
        StringBuilder sb = new StringBuilder();

        sb.append(FileUtils.readFileToString(zxgFile));

        for (String code : limit) {
            sb.append(code + "\r\n");
        }
        FileUtils.writeStringToFile(zxgFile, sb.toString());
        System.err.println(limit.toString());
    }
}
