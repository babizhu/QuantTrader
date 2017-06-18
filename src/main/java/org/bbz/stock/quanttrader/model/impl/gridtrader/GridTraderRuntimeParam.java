package org.bbz.stock.quanttrader.model.impl.gridtrader;

import lombok.Data;
import org.bbz.stock.quanttrader.stock.StockInfo;
import org.bbz.stock.quanttrader.stock.StockTraderRecord;

import java.util.List;

/**
 * Created by liukun on 2017/6/17.
 * 用户的运行时参数
 */
@Data
public class GridTraderRuntimeParam{
    /**
     * 为整个模型准备的初始资金，永远不会被改变
     */
    private float initAmount;

    /**
     * 当前的可用资金
     */
    private float currentAmount;
    /**
     * 整个过程中的交易信息，因为没有相应的交易接口，目前只能手动录入
     */
    private List<StockTraderRecord> traderRecords;
    /**
     * 当前持有的股票数量
     */
    private StockInfo stockInfo;

    /**
     * 交易成功之后，修改持仓以及现金情况
     *
     * @param traderRecord 交易信息
     */
    public void trade( StockTraderRecord traderRecord ){

        float amount = traderRecord.getCount() * traderRecord.getPrice();
//        stockInfo.trade(traderRecord);
        if( traderRecord.isBuy() ) {
            stockInfo.incrementCount( traderRecord.getCount() );
            currentAmount -= amount;
        } else {
            stockInfo.decrementCount( traderRecord.getCount() );
            currentAmount += amount;
        }
        traderRecords.add( traderRecord );
    }

    private double calcFee( float tradeFee ){
        return traderRecords.stream().mapToDouble( v -> v.getPrice() * v.getCount() * tradeFee ).sum();
    }

    /**
     * 计算盈利，要减去手续费
     *
     * @param stockCurrentPrice 股票的当前价格
     * @param traderFee         交易手续费
     * @return
     */
    public float calcProfit( float stockCurrentPrice, float traderFee ){
        float amount = stockInfo.getCount() * stockCurrentPrice;//当前市值
        amount += currentAmount;

        //计算手续费
        amount -= calcFee( traderFee );

        amount -= initAmount;
        return amount;
    }

    public String print( float stockCurrentPrice, float traderFee ){

        StringBuilder sb = new StringBuilder( toString() );
        sb.deleteCharAt( sb.length() - 1 );
        sb.append( ", 手续费=" ).append( calcFee( traderFee ) )
                .append( ", 盈利=" ).append( calcProfit( stockCurrentPrice, traderFee ) )
                .append( "}" );

        return sb.toString();
    }

    @Override
    public String toString(){
        return "GridTraderRuntimeParam{" +
                "initAmount=" + initAmount +
                ", currentAmount=" + currentAmount +
                ", traderRecords=" + traderRecords +
                ", stockInfo=" + stockInfo +
                '}';
    }
}
