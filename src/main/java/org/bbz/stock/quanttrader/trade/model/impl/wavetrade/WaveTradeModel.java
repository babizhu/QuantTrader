package org.bbz.stock.quanttrader.trade.model.impl.wavetrade;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.KLineType;
import org.bbz.stock.quanttrader.trade.core.Portfolio;
import org.bbz.stock.quanttrader.trade.core.QuantTradeContext;
import org.bbz.stock.quanttrader.trade.financeindicators.FinanceIndicators;
import org.bbz.stock.quanttrader.trade.model.AbstractTradeModel;
import org.bbz.stock.quanttrader.trade.stockdata.IStockDataProvider;
import org.bbz.stock.quanttrader.trade.tradehistory.SimpleKBar;
import org.bbz.stock.quanttrader.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liulaoye on 17-7-6.
 * 卢根 提供的策略
 */
@Slf4j
public class WaveTradeModel extends AbstractTradeModel{
    private final QuantTradeContext ctx;
    private final IStockDataProvider dataProvider;

    public WaveTradeModel( QuantTradeContext ctx, IStockDataProvider dataProvider ){
        this.ctx = ctx;
        this.dataProvider = dataProvider;
    }

    @Override
    public void run( Long aLong ){
        log.info( "开始执行策略: " + DateUtil.formatDateTime( LocalDateTime.now() ) );
        lastRunInfo = "开始执行策略: " + DateUtil.formatDateTime( LocalDateTime.now()  ) + "</br>";
        final Portfolio portfolio = ctx.getPortfolio();
        for( Map.Entry<String, Integer> stock : portfolio.getStocks().entrySet() ) {
            cleanUp( stock.getKey() );
            if( stock.getValue() == 0 ) {
                checkFirstBuy( stock.getKey() );
            } else {
                checkSellOrBuyInLittleWave( stock.getKey() );//在小波浪中考虑加减仓
            }
        }
    }

    /**
     * 小波段中检测加减仓条件<br/>
     * 上一次交易记录是买，这次才能卖<br/>
     * 上一次交易记录是卖，这次才能买<br/>
     * <p>
     * 首次在哪个k线周期买入，以后所有的小波段操作都应该在这个k线周期完成<br/>
     */
    private void checkSellOrBuyInLittleWave( String stockId ){
        final JsonObject attachement = attachements.get( stockId );
        final int lastOp = attachement.getInteger( Consts.LAST_OP_IN_LITTLE_WAVE_KEY );
        KLineType kLineType = KLineType.fromString( attachement.getString( Consts.KLINE_TYPE_KEY ) );
        if( lastOp == Consts.BUY ) {//小波段减仓
            checkSellInLittleWave( stockId, kLineType );
        } else {//小波段加仓
            checkBuyInLittleWave( stockId, kLineType );
        }
    }

    /**
     * 检测首次买入
     *
     * @param stockId stockId
     */
    private void checkFirstBuy( String stockId ){
        checkWeekUp( stockId )
                .compose( res -> check60( stockId, true ) )
                .setHandler( res -> {
                    String result = stockId + " : ";
                    if( res.failed() ) {
                        result += res.cause().getMessage();
                    } else {
                        result += "买入";
                        ctx.order( stockId, 200 );
                        KLineType kLineType = res.result().getkLineType();
                        setAttachement( stockId, Consts.KLINE_TYPE_KEY, kLineType.toStr() );
                        setAttachement( stockId, Consts.FIRST_BUY_DATE_KEY, DateUtil.formatDate( ctx.getCurrentDate() ) );
                        setFirstCleanupPrice( stockId, LocalDate.now() );
                    }
                    System.out.println( result );
                    lastRunInfo += result+"<br/>";
                } );
    }

    /**
     * 设置第一个清仓点
     *
     * @param date
     */
    private void setFirstCleanupPrice( String stockId, LocalDate date ){
        dataProvider.getSimpleKBar( stockId, KLineType.DAY, 100, date.plusDays( -1 ), date, res -> {
            if(res.succeeded()){
                setAttachement( stockId, Consts.CLEAN_UP_PRICE_KEY, res.result().get( 0 ).getLow() );
            }else {
                log.error( res.cause().getMessage());
            }
        });
    }

    private void checkSellInLittleWave( String stockId, KLineType kLineType ){
        dataProvider.getSimpleKBar( stockId, kLineType, 100, res -> {
            String result = stockId + " : ";
            if( res.succeeded() ) {
                final List<SimpleKBar> kBars = res.result();
                if( KValueGreaterThan( kBars, 80 ) ) {//K值 > 80
                    if( checkDown( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {
                        result += "卖出";
                        ctx.order( stockId, -200 );
                        setAttachement( stockId, Consts.LAST_OP_IN_LITTLE_WAVE_KEY, Consts.SELL );
                    } else {
                        result += kLineType + "线未形成下摆,不能卖出";
                    }
                } else {
                    result += kLineType + "k值 < 80 ,不能卖出";
                }
            } else {
                result += res.cause().getMessage();
            }
            System.out.println( result );
            lastRunInfo += result+"<br/>";
        } );
    }

    /**
     * 判断周线是否上摆
     *
     * @param stockId 股票id
     * @return Future<Boolean>
     */
    private Future<Void> checkWeekUp( String stockId ){
        return Future.<List<SimpleKBar>>future( f -> dataProvider.getSimpleKBar( stockId, KLineType.WEEK, 2, f )
        ).compose( kBars -> {
            if( checkUp( kBars ) ) {
                return Future.succeededFuture();
            } else {
                return Future.failedFuture( "周线未形成上摆" );
            }
        } );
    }

    /**
     * 判断小波段的股票加仓的条件
     * 外层确保此股票的当前数量为0
     *
     * @param stockId   stock id
     * @param kLineType 操作的K线周期
     */
    private void checkBuyInLittleWave( String stockId, KLineType kLineType ){
        final Future<CheckResult> future;
        if( kLineType == KLineType.MIN60 ) {
            future = check60( stockId, false );
        } else {
            future = check30( stockId );
        }
        future.setHandler( res -> {
            String result = stockId + " : ";
            if( res.failed() ) {
                result += res.cause().getMessage();
            } else {
                result += "买入";
                ctx.order( stockId, 200 );
                setAttachement( stockId, Consts.LAST_OP_IN_LITTLE_WAVE_KEY, Consts.BUY );
            }
            System.out.println( result );
            lastRunInfo += result+"<br/>";
        } );
    }

    /**
     * 检测60分钟k线的购买条件
     *
     * @param stockId     stockId
     * @param needCheck30 60分钟检测失败，是否需要检测30分钟
     * @return 成功：Future.succeededFuture()
     * 失败：Future.failedFuture( "失败原因" )
     */
    private Future<CheckResult> check60( String stockId, boolean needCheck30 ){
        return Future.<List<SimpleKBar>>future( f -> dataProvider.getSimpleKBar( stockId, KLineType.MIN60, 100, f )
        ).compose( kBars -> {
            if( KValueLessThan( kBars, 35 ) ) {
//              System.out.println("60分钟K值小于35，进入下一个检测");
                if( checkUp( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {//检测60分钟是否形成上摆
                    return Future.succeededFuture( CheckResult.createResult( KLineType.MIN60 ) );
                } else {
                    if( needCheck30 ) {
                        return check30( stockId );
                    } else {
                        return Future.failedFuture( "60分钟K值 未小于 35" );
                    }
                }
            } else {
                return Future.failedFuture( "60分钟K值 未小于 35" );
            }
        } );
    }

    /**
     * 检测30分钟k线的购买条件
     *
     * @param stockId stockId
     * @return 成功：Future.succeededFuture()
     * 失败：Future.failedFuture( "失败原因" )
     */
    private Future<CheckResult> check30( String stockId ){
        return Future.<List<SimpleKBar>>future( f -> dataProvider.getSimpleKBar( stockId, KLineType.MIN30, 100, f )
        ).compose( kBars -> {
            if( KValueLessThan( kBars, 35 ) ) {
                if( checkUp( kBars.subList( kBars.size() - 2, kBars.size() ) ) ) {//检测30分钟是否形成上摆
                    return Future.succeededFuture( CheckResult.createResult( KLineType.MIN30 ) );
                } else {
                    return Future.failedFuture( "30分钟K线未形成上摆" );
                }
            } else {
                return Future.failedFuture( "30分钟K值 未小于 35" );
            }
        } );
    }

    @Override
    public void beforeOpen(){
        System.out.println( "WaveTradeModel.beforeOpen!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }

    @Override
    public void afterClose(){
        log.info( "开始执行盘后策略: " + DateUtil.formatDateTime( LocalDateTime.now() ) );
        final Portfolio portfolio = ctx.getPortfolio();
        for( Map.Entry<String, Integer> stock : portfolio.getStocks().entrySet() ) {
            if( stock.getValue() != 0 ) {
                calcCleanPriceInBigWave( stock.getKey() );
            }
        }
    }



    /**
     * 判断大波段的股票加仓和清仓价格
     *
     * @param stockId stockId
     */
    void calcCleanPriceInBigWave( String stockId ){
        dataProvider.getSimpleKBar( stockId, KLineType.DAY, 100, res -> {
            if( res.succeeded() ) {
                List<SimpleKBar> result = res.result();
                List<SimpleKBar> subList = result.stream().filter( v -> v.getTime().toLocalDate().isAfter( LocalDate.parse( "2017-06-06" ) ) ).collect( Collectors.toList() );
                SimpleKBar current = subList.get( 0 );
                double lowPrice = Double.MAX_VALUE;
                double highPrice = Double.MIN_VALUE;

                boolean isDown = false;
                for( int i = 1; i < subList.size(); i++ ) {
                    SimpleKBar temp = subList.get( i );
                    if( current.getLow() > temp.getLow() && current.getHigh() > temp.getHigh() ) { //下摆
                        if( !isDown ) {
                            System.out.println( "高点 ：" + highPrice );
                        }
                        isDown = true;
                        lowPrice = Math.min( lowPrice, temp.getLow() );//记录下摆中的最低点
                        highPrice = Double.MIN_VALUE;
                        current = temp;
                    } else if( current.getLow() < temp.getLow() && current.getHigh() < temp.getHigh() ) { //上摆
                        if( isDown ) {
                            System.out.println( current.getTime() + " 低点 ：" + lowPrice );
                        }
                        isDown = false;
                        highPrice = Math.max( highPrice, temp.getHigh() );//记录下摆中的最低点
                        lowPrice = Double.MAX_VALUE;
                        current = temp;
                    } else {
                        if( isDown ) {
                            lowPrice = Math.min( lowPrice, temp.getLow() );//记录下摆中的最低点
                        } else {
                            highPrice = Math.max( highPrice, temp.getHigh() );//记录上摆中的最高点
                        }
                    }
                }
                setAttachement( stockId, Consts.CLEAN_UP_PRICE_KEY, lowPrice );
            } else {
                res.cause().printStackTrace();
            }

        } );
//        double[][] data = {{18.55, 17.39}, {19.17, 18.50}, {19.10, 18.65},
//                {19.45, 19.00}, {19.64, 19.22}, {19.40, 18.74}, {19.05, 18.68},
//                {20.14, 18.62}, {20.37, 19.57}, {21.80, 19.75}, {23.07, 21.50},
//                {22.30, 21.57}, {21.99, 21.46}, {21.86, 21.22}, {21.50, 20.91},
//                {22.20, 20.53}, {22.59, 21.66}, {23.49, 22.11}, {23.36, 22.34},
//                {24.65, 22.88}, {24.77, 23.48}, {24.50, 23.60}, {23.60, 22.68},
//                {25.00, 22.80}, {25.73, 24.32}, {26.76, 24.14}};
//        //                      0       买       孕      外      上       下      下      上
////        double[][] data = {{10,20},{20,30},{22,28},{19,31},{22,32},{21,30},{19,29},{21,30}};
//        double[] current = data[0];
////        double lowPrice = current[0];
//        double lowPrice = 1000000;
//        double highPrice = current[1];
//
//        boolean xb = false;
//        for( int i = 1; i < data.length; i++ ) {
//
//            if( current[0] > data[i][0] && current[1] > data[i][1] ) {//下摆
////                System.out.println(data[i][0] + ":" + data[i][1]);
//                if( !xb ) {
//                    System.out.println( "高点 ：" + highPrice );
//                }
//                xb = true;
//                lowPrice = Math.min( lowPrice, data[i][1] );//记录下摆中的最低点
//                highPrice = 0;
//                current = data[i];
//            } else if( current[0] < data[i][0] && current[1] < data[i][1] ) {//上摆
//                if( xb ) {
//                    System.out.println( "低点 ：" + lowPrice );
//                }
//                xb = false;
//                highPrice = Math.max( highPrice, data[i][0] );//记录下摆中的最低点
//                lowPrice = 100000;
//                current = data[i];
//            } else {
//                if( xb ) {
//                    lowPrice = Math.min( lowPrice, data[i][1] );//记录下摆中的最低点
//
//                } else {
//                    highPrice = Math.max( highPrice, data[i][0] );//记录下摆中的最低点
//
//                }
//            }
//
//        }
//        return null;
    }

    /**
     * 检测D值是否小于某个数值
     *
     * @param data k线序列
     * @param v    要比较的值
     * @return true:   小于参数v          false:  大于参数v
     */
    @SuppressWarnings("SameParameterValue")
    private boolean KValueLessThan( List<SimpleKBar> data, double v ){
        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( data, 8, 3, 2 );
        int len = data.size() - 1;
        return doubles[0][len] < v;
    }

    /**
     * 检测kdj指标中的D值是否大于某个数值
     *
     * @param data k线序列
     * @param v    要比较的值
     * @return true:   大于参数v          false:  小于参数v
     */
    @SuppressWarnings("SameParameterValue")
    private boolean KValueGreaterThan( List<SimpleKBar> data, double v ){
        double[][] doubles = FinanceIndicators.INSTANCE.calcKDJ( data, 8, 3, 2 );
        int len = data.size() - 1;
//        System.out.println( "k:" + doubles[0][len] );
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
            log.error( "判断上摆的数据不等于2个" );
            return false;
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
            log.error( "判断下摆的数据不等于2个,数量为：" + data.size() );
            return false;
        }
        final SimpleKBar oldSimpleKBar = data.get( 0 );
        final SimpleKBar newSimpleKBar = data.get( 1 );
        return oldSimpleKBar.getLow() > newSimpleKBar.getLow() && oldSimpleKBar.getHigh() > newSimpleKBar.getHigh();
    }

    /**
     * 当前股价低于清仓价
     * 执行清仓
     *
     * @param stockId stockId
     */
    private void cleanUp( String stockId ){
        Double cleanupPrice = getDoubleFromAttachements( stockId, Consts.CLEAN_UP_PRICE_KEY );
        if( cleanupPrice == null ) {
            return;
        }
        dataProvider.getCurrentKbar( stockId, res -> {
            if( cleanupPrice > res.getClose() ) {
                System.out.println( "当前价(" + res + ")低于清仓点：" + cleanupPrice + "。清仓卖出！！！" );
                ctx.cleanUp( stockId );
            }
        } );
    }
}