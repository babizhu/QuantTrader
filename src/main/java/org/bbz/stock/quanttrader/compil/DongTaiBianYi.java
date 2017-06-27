package org.bbz.stock.quanttrader.compil;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by liu_k on 2017/6/26.
 */


public class DongTaiBianYi{

    /**
     * @param args
     * @throws Exception ￥-824.40 -824.40
     */
    public static void main( String[] args ) throws Exception{
        // TODO Auto-generated method stub
        DongTaiBianYi dtby = new DongTaiBianYi();
        //dtby.textJavaCompiler();
        dtby.textStringWrite();

    }

    public void textJavaCompiler() throws IOException{
        // 编译程序
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        int result = javaCompiler.run( null, null, null, "-d", "./src/", "./src/file/MyText.java" );
        System.out.println( result == 0 ? "恭喜编译成功" : "对不起编译失败" );

    }

    public void textStringWrite() throws Exception{
        JavaCompiler complier = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager sjf =
                complier.getStandardFileManager( null, null, null );
        Iterable it = sjf.getJavaFileObjects( "D:/Hello.java" );
        CompilationTask task = complier.getTask( null, sjf, null, null, null, it );
        task.call();  //调用创建  ,创建class文件
        sjf.close();

        URL urls[] = new URL[]{new URL( "file:/D:/" )}; //储存文件目录的地址
        URLClassLoader uLoad = new URLClassLoader( urls );  //classloader从哪个目录找？
        //uLoad.loadClass("Hello");
        Class c = uLoad.loadClass( "Hello" );  //找哪个class文件 注意不带后缀名
        Object o = c.newInstance();//创建一个实例


        Method method = c.getMethod( "printString" );
        String string = method.invoke( o ).toString();

        System.out.println( string );
    }


}