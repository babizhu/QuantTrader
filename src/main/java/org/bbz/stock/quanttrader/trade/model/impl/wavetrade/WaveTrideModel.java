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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by liulaoye on 17-7-6.
 * 卢根 提供的策略
 */
@Slf4j
public class WaveTrideModel extends AbstractTradeModel{
    private final QuantTradeContext ctx;
    private final IStockDataProvider dataProvider;

    public WaveTrideModel( QuantTradeContext ctx, IStockDataProvider dataProvider ){
        this.ctx = ctx;
        this.dataProvider = dataProvider;
    }

    @Override
    public void run( Long aLong ){
        log.info( "开始执行策略: " + DateUtil.formatDateTime( LocalDateTime.now() ) );
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
                    }
                    System.out.println( result );
                } );
    }

    private void checkSellInLittleWave( String stockId, KLineType kLineType ){
        dataProvider.getSimpleKBarExt( stockId, kLineType, 100, res -> {
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
        } );
    }

    /**
     * 判断周线是否上摆
     *
     * @param stockId 股票id
     * @return Future<Boolean>
     */
    private Future<Void> checkWeekUp( String stockId ){
        return Future.<List<SimpleKBar>>future( f -> dataProvider.getSimpleKBarExt( stockId, KLineType.WEEK, 2, f )
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
        return Future.<List<SimpleKBar>>future( f -> dataProvider.getSimpleKBarExt( stockId, KLineType.MIN60, 100, f )
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
        return Future.<List<SimpleKBar>>future( f -> dataProvider.getSimpleKBarExt( stockId, KLineType.MIN30, 100, f )
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
    }

    @Override
    public void afterClose(){
        log.info( "开始执行盘后策略: " + DateUtil.formatDateTime( LocalDateTime.now() ) );
        final Portfolio portfolio = ctx.getPortfolio();
        for( Map.Entry<String, Integer> stock : portfolio.getStocks().entrySet() ) {
            if( stock.getValue() != 0 ) {
                double[] priceInBigWave = calcCleanPriceInBigWave( stock.getKey() );
            }
        }
    }

    /**
     * 判断大波段的股票加仓和清仓价格
     *
     * @param stockId stockId
     * @return 用一个数组保存加仓和减仓的价格.
     * [0]:加仓价格
     * [1]:清仓价格
     */
    private double[] calcCleanPriceInBigWave( String stockId ){
        return null;
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
     * 检测D值是否大于某个数值
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

    /**
     * 当前股价低于清仓价
     * 执行清仓
     *
     * @param stockId stockId
     */
    private void cleanUp( String stockId ){
        dataProvider.getCurrentPrice( stockId, res -> {
            Double cleanupPrice = attachements.get( stockId ).getDouble( Consts.CLEAN_UP_PRICE_KEY );
            if( cleanupPrice > res.result() ) {
                System.out.println( "当前价(" + res.result() + ")低于清仓点：" + cleanupPrice + "。卖出" );
                ctx.cleanUp( stockId );
            }
        } );
    }
}