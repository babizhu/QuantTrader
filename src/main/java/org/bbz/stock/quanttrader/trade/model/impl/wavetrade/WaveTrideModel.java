package org.bbz.stock.quanttrader.trade.model.impl.wavetrade;

import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.trade.core.Portfolio;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.financeindicators.FinanceIndicators;
import org.bbz.stock.quanttrader.trade.model.ITradeModel;
import org.bbz.stock.quanttrader.trade.stock.StockTraderRecord;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by liulaoye on 17-7-6.
 * 卢根 提供的策略
 */
@Slf4j
public class WaveTrideModel implements ITradeModel{

    private final QuantTradeContext ctx;
    private final IStockDataProvider dataProvider;


    public WaveTrideModel( QuantTradeContext ctx, IStockDataProvider dataProvider ){
        this.ctx = ctx;
        this.dataProvider = dataProvider;
    }

    @Override
    public void initialize(){

    }

    @Override
    public void run( Long aLong ){
        System.out.println( "开始执行策略" + LocalDateTime.now() );
        final Portfolio portfolio = ctx.getPortfolio();
        for( Map.Entry<String, Integer> stock : portfolio.getStocks().entrySet() ) {
            if( stock.getValue() == 0 ) {
                checkBuyByLittleWave( stock.getKey(), true );
            } else {
                checkSellOrBuy( stock.getKey() );
            }
        }
//        for( int i = 600000; i < 601000; i++ ) {
//        }


//        checkBuyByLittleWave( "600116" );

    }


//    /**
//     * 获取某只股票同一个unit上的上一次交易记录
//     *
//     * @param stockId       股票id
//     * @param kUnit         k线周期
//     * @return 交易记录
//     */
//    private StockTraderRecord getLastTraderRecord( String stockId, String kUnit ){
//        final List<StockTraderRecord> traderRecords = ctx.getTraderRecordsByStockId( stockId );
//        final List<StockTraderRecord> stockTraderRecordList = traderRecords.stream().
//                filter( v -> v.getAttachement().getString( StockTraderRecord.BUY_POINT_KUNIT ).equals( kUnit ) ).
//                collect( Collectors.toList() );
//        return stockTraderRecordList.get( stockTraderRecordList.size() - 1 );
//    }

    /**
     * 检测加减仓条件
     * 上一次交易记录是买，这次才能卖，反过来也一样
     *
     * @param stockId stockId
     */
    private void checkSellOrBuy( String stockId ){
        final List<StockTraderRecord> traderRecords = ctx.getTraderRecordsByStockId( stockId );
        if( traderRecords.size() == 0 ) {
            log.error( "未找到购买记录" );
            return;
        }
        final StockTraderRecord lastRecord = traderRecords.get( traderRecords.size() - 1 );
        String buyPointKType = lastRecord.getAttachement().getString( StockTraderRecord.BUY_POINT_KUNIT );//上次买入的k线单位

        if( lastRecord.isBuy() ) {//小波段减仓
            dataProvider.getSimpleKBarExt( stockId, buyPointKType, 100, res -> {
                if( res.succeeded() ) {
                    final List<SimpleKBar> kBars = res.result();
                    if( KValueGreaterThan( kBars, 80 ) ) {//K值 > 80
                        if( checkDown( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {
                            System.out.println( "卖出!!!!!!!!!!!!!!!!!!!!!" );
                        } else {
                            System.out.println( "未形成下摆,不能卖出" );
                        }
                    } else {
                        System.out.println( "k值 < 80 ,不能卖出" );
                    }
                } else {
                    res.cause().printStackTrace();
                }
            } );
        } else {//小波段加仓

        }


    }


    /**
     * 判断小波段的股票买入的条件
     * 首次购买需考虑周线上摆的条件
     * 外层确保此股票的当前数量为0
     *
     * @param stock 股票id
     */
    private void checkBuyByLittleWave( String stock, boolean isFirstBuy ){

        Future.<List<SimpleKBar>>future( f -> {
                    dataProvider.getSimpleKBarExt( stock, "W", 2, f );
//                    System.out.print( "获取周k线数据" );
                }
        ).compose( kBars -> {

            if( checkUp( kBars ) ) {
//                System.out.println( "周k线数据上摆成功" );
//                System.out.print( "获取周60分钟k线数据" );

                return Future.future( f -> dataProvider.getSimpleKBarExt( stock, "60", 100, f ) );
            } else {
//                System.out.println( "周k线数据未形成上摆" );
                return Future.<List<SimpleKBar>>failedFuture( "周线未形成上摆" );
            }
        } ).compose( kBars -> {
//            System.out.println("\t\t 成功");
            if( KValueLessThan( kBars, 35 ) ) {
//                System.out.println("60分钟K值小于35，进入下一个检测");

                if( checkUp( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {//检测60分钟是否形成上摆
//                    System.out.println("60分钟条件成立，返回成功future，可以买入");
                    return Future.succeededFuture();
                } else {//60分钟未形成上摆
//                    System.out.println("60分钟k线未形成上摆，进入30分钟检测");
                    return check30( stock );
                }
            } else {
//                System.out.println( "60分钟k线的K值未小于35，返回失败future" );
                return Future.failedFuture( "60分钟K值 未小于 35" );
            }
        } ).setHandler( res -> {
            String result = stock + " : ";
            if( res.failed() ) {
//                res.cause().printStackTrace();
                result += res.cause().getMessage();
            } else {
                result += "买入";
//                System.out.println( "setHandler: " + res.result() );
            }
            System.out.println( result );

        } );

    }

    /**
     * 检测30分钟k线的情况
     */
    private Future<List<SimpleKBar>> check30( String stock ){
        return Future.<List<SimpleKBar>>future( f -> {
                    dataProvider.getSimpleKBarExt( stock, "30", 100, f );
//                    System.out.print( "获取30分钟k线数据" );
                }
        ).compose( kBars -> {
//            System.out.println("\t\t 成功");
            if( KValueLessThan( kBars, 35 ) ) {
//                System.out.println("30分钟K值小于35，进入下一个检测");

                if( checkUp( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {//检测30分钟是否形成上摆
//                    System.out.println("30分钟条件成立，返回成功future，可以买入");
                    return Future.succeededFuture();
                } else {
//                    System.out.println("30分钟k线未形成上摆，返回失败future");

                    return Future.failedFuture( "30分钟K线 未形成上摆" );
                }
            } else {
//                System.out.println("30分钟k线的D值未小于35，返回失败future");

                return Future.failedFuture( "30分钟K值 未小于 35" );
            }
        } );
    }

    @Override
    public void beforeOpen(){

    }

    @Override
    public void afterClose(){

    }

    /**
     * 检测D值是否小于某个数值
     *
     * @param data k线序列
     * @param v    要比较的值
     * @return true:   小于参数v          false:  大于参数v
     */

    private boolean KValueLessThan( List<SimpleKBar> data, double v ){
        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( data, 8, 3, 2 );
        int len = data.size() - 1;
        return doubles[0][len] < v;
    }

    /**
     * 检测D值是否大于某个数值
     *
     * @param data k线序列
     * @param v    要比较的值
     * @return true:   大于参数v          false:  小于参数v
     */

    private boolean KValueGreaterThan( List<SimpleKBar> data, double v ){
        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( data, 8, 3, 2 );
        int len = data.size() - 1;
        System.out.println( "k:" + doubles[0][len] );
        return doubles[0][len] > v;
    }


    /**
     * 判断两个k bar 是否形成上摆
     *
     * @return true:   形成上摆
     * false:  未形成上摆
     */
    private boolean checkUp( List<SimpleKBar> data ){
        if( data.size() != 2 ) {
            log.warn( "判断上摆的数据不等于2个" );
        }
        final SimpleKBar oldSimpleKBar = data.get( 0 );
        final SimpleKBar newSimpleKBar = data.get( 1 );
        return oldSimpleKBar.getLow() < newSimpleKBar.getLow() && oldSimpleKBar.getHigh() < newSimpleKBar.getHigh();
    }


    /**
     * 判断两个k bar 是否形成下摆
     *
     * @return true:   形成下摆
     * false:  未形成上摆
     */
    private boolean checkDown( List<SimpleKBar> data ){
        if( data.size() != 2 ) {
            log.warn( "判断下摆的数据不等于2个,数量为：" + data.size() );
        }
        final SimpleKBar oldSimpleKBar = data.get( 0 );
        final SimpleKBar newSimpleKBar = data.get( 1 );
        return oldSimpleKBar.getLow() > newSimpleKBar.getLow() && oldSimpleKBar.getHigh() > newSimpleKBar.getHigh();
    }
}
