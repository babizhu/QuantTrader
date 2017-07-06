package org.bbz.stock.quanttrader.model.impl.wavetrade;

import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.core.QuantTradeContext;
import org.bbz.stock.quanttrader.financeindicators.FinanceIndicators;
import org.bbz.stock.quanttrader.model.ITradeModel;
import org.bbz.stock.quanttrader.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.tradehistory.SimpleKBar;

import java.util.List;

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
//        final Portfolio portfolio = ctx.getPortfolio();
//        for( Map.Entry<String, Integer> stock : portfolio.getStocks().entrySet() ) {
//            if( stock.getValue() == 0 ) {
//                checkFirstBuy( stock.getKey() );
//            }
//        }
        checkFirstBuy( "600097" );

    }

    /**
     * 判断首次买入的条件
     * 外层确保此股票的当前数量为0
     *
     * @param stock
     */
    private void checkFirstBuy( String stock ){

        Future.<List<SimpleKBar>>future( f ->{
                dataProvider.getSimpleKBarExt( stock, "W", 2, f );
                    System.out.print( "获取周k线数据" );
        }
        ).compose( kBars -> {
            System.out.println( "\t\t成功" );

            if( checkUp( kBars ) ) {//####
                System.out.println( "周k线数据上摆成功" );
                System.out.print( "获取周60分钟k线数据" );

                return Future.future( f -> dataProvider.getSimpleKBarExt( stock, "60", 100, f ) );
            } else {
                System.out.println( "周k线数据未形成上摆" );
                Future<List<SimpleKBar>> failResult = Future.failedFuture( "周线未形成上摆" );
                return failResult;
            }
        } ).compose( kBars -> {
            System.out.println("\t\t 成功");
            if( KValueLessThan( kBars, 35 ) ) {
                System.out.println("60分钟K值小于35，进入下一个检测");

                if( checkUp( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {//检测60分钟是否形成上摆
                    System.out.println("60分钟条件成立，返回成功future，可以买入");
                    Future<List<SimpleKBar>> successResult = Future.succeededFuture();
                    return successResult;
                } else {//60分钟未形成上摆
                    System.out.println("60分钟k线未形成上摆，进入30分钟检测");

                    return check30( stock );
                }
            } else {
                System.out.println("60分钟k线的D值未小于35，返回失败future");

                Future<List<SimpleKBar>> failResult = Future.failedFuture( "60分钟K值 未小于 35" );
                return failResult;
            }
        } ).setHandler( res -> {
            System.out.println("进入最后集中处理");
            if( res.failed() ) {
                res.cause().printStackTrace();
            } else {
                System.out.println( "买入" );
                System.out.println( "setHandler: " + res.result() );
            }
        } );

    }

    /**
     * 检测30分钟k线的情况
     */
    private Future<List<SimpleKBar>> check30( String stock ){
        return Future.<List<SimpleKBar>>future( f -> {
                    dataProvider.getSimpleKBarExt( stock, "30", 100, f );
                    System.out.print( "获取30分钟k线数据" );
                }
        ).compose( data -> {
            System.out.println("\t\t 成功");
            if( KValueLessThan( data, 35 ) ) {
                System.out.println("30分钟K值小于35，进入下一个检测");

                if( checkUp( data.subList( data.size() - 2, data.size() ) ) ) {//检测30分钟是否形成上摆
                    System.out.println("30分钟条件成立，返回成功future，可以买入");
                    Future<List<SimpleKBar>> successResult = Future.succeededFuture();
                    return successResult;
                } else {
                    System.out.println("30分钟k线未形成上摆，返回失败future");

                    Future<List<SimpleKBar>> failResult = Future.failedFuture( "30分钟K线 未形成上摆" );
                    return failResult;
                }
            } else {
                System.out.println("30分钟k线的D值未小于35，返回失败future");

                Future<List<SimpleKBar>> failResult = Future.failedFuture( "30分钟K值 未小于 35" );
                return failResult;
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
//        System.out.println( "FinanceIndicatorsTest.KValueLessThan:" + data );
        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( data );
        int len = data.size() - 1;
//        System.out.println( "K:" + doubles[0][len] + ", D:" + doubles[1][len] + ", J:" + doubles[2][len] );

        return doubles[0][len] < v;
//        return false;
    }


    /**
     * 判断两个k bar 是否形成上摆
     *
     * @return true:   形成上摆
     * false:  未形成上摆
     */
    private boolean checkUp( List<SimpleKBar> data ){
        if( data.size() != 2 ) {
            log.warn( "判断上摆的数据多余2个" );
        }
        final SimpleKBar oldSimpleKBar = data.get( 0 );
        final SimpleKBar newSimpleKBar = data.get( 1 );

        return oldSimpleKBar.getLow() < newSimpleKBar.getLow() && oldSimpleKBar.getHigh() < newSimpleKBar.getHigh();
//        return false;//####
    }

}
