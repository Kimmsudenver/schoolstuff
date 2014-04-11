package com.hygenics.crawlerclient;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.UdtMessage;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.Calendar;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.hygenics.crawlerclient.CustomUtilThreadFactory;
import com.hygenics.sftp.SFTP;

/**
 * DECODER is MessageDecoder
 */

public class CrawlerClient {
	
	 private static final Logger log = LoggerFactory.getLogger(CrawlerClient.class);

	    private final String host;
	    private final int port;
	    private final MessageObject msgObject= new MessageObject();


	    public CrawlerClient(final String host, final int port) {
	        this.host = host;
	        this.port = port;
	        log.info("Received Host: "+this.host+" Port: "+this.port);
	    }
	    
	    public class NettyClientHandler extends SimpleChannelInboundHandler<UdtMessage> {
	    	private final Logger log = LoggerFactory.getLogger("logger");
	    	private final int messageSize;
	    	private final UdtMessage message;
	    	//shared memory between client and client handler--> no need to lock for now
	    	private  MessageObject msgObject;
	    	
	    	
	    	public NettyClientHandler(final int messageSize,String outmessage,final MessageObject mo)
	    	{
	    		
	    	final ByteBuf bytebuf=Unpooled.buffer(outmessage.length());
	      	   this.messageSize=messageSize;
	      	   this.msgObject=mo;
	      	   bytebuf.writeBytes((outmessage+"\n").getBytes());
	      	   message=new UdtMessage(bytebuf);
	    	}


	    	@Override
	        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
	            //log.info("ECHO active " + NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
	    		log.info("ECHO active " + NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
	            log.info("Prepping Write "+this.messageSize);
	            ctx.writeAndFlush(message);
	            log.info("Write Complete");
	 
	        }

	        @Override
	        public void exceptionCaught(final ChannelHandlerContext ctx,final Throwable cause) {
	            log.warn("Exception Raised: Closing Connection", cause);
	            ctx.close();
	        }

	        @Override
	        public void channelRead0(ChannelHandlerContext ctx, UdtMessage msg) throws Exception {
	        	//TODO GET THE INPUT BACK FROM THE SERVER
	        	log.info("reading");
	        	String temp="";
	        	
	        	ByteBuf buf=((UdtMessage) msg).content();
	        	
	        	for(int i=0;i<buf.readableBytes();i++)
	        	{
	        		temp+=(char)buf.getByte(i);
	        	}
	        	
	        	msgObject.setMessage(temp);
	        	
	        	log.info("MESSAGE RECEIVED:"+temp);
	        }

	        @Override
	        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	            ctx.flush();
	            log.info("CLOSING");
	        	ctx.close();
	        }

	    }

	    public String run(final String message) throws Exception{
	    	
	    	runClient(message);
	    	
	    	return msgObject.getMessage();
	    }
	    
	    
	    public void runClient(final String message) throws Exception {
	    	log.info("PREPPING Client: "+message);
	    	
	    	InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
	        // Configure the client
	        final ThreadFactory connectFactory = new CustomUtilThreadFactory("connect");
	        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,connectFactory, NioUdtProvider.MESSAGE_PROVIDER);
	        
	        
	        msgObject.setMessage(null);
	        
	        try {
	            final Bootstrap boot = new Bootstrap();
	            boot.group(connectGroup)
	            		
	                    .channelFactory(NioUdtProvider.MESSAGE_CONNECTOR)
	              
	                    .handler(new ChannelInitializer<UdtChannel>() {
	                        @Override
	                        public void initChannel(final UdtChannel ch) throws Exception {
	                        try{
	                        	ch.pipeline().addFirst(new LoggingHandler("logger"));
	   							ch.pipeline().addLast(new NettyClientHandler(message.length(),message,msgObject));
	   							ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(200));
	   							ch.pipeline().addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
	   							ch.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));

	   						}catch(Exception e)
	   						{
	   							log.info(e.getMessage());
	   							e.printStackTrace();
	   						}
	                        }
	                    });
	            // Start the client.
	            final ChannelFuture f = boot.connect(this.host, this.port).sync();
	           
	            // Wait until the connection is closed.
	            f.channel().closeFuture().sync();
	        } finally {
	            // Shut down the event loop to terminate all threads.
	            connectGroup.shutdownGracefully();
	        }
	        log.info("CLIENT COMPLETED ON: "+Calendar.getInstance().getTime().toString());
	    }
	
			
			public void upload(byte[] bytes, String inname)
			{
				log.info("UPLOADING "+inname);
				uploadImage(bytes,inname);
				log.info("UPLOADED ON"+Calendar.getInstance().getTime().toString());
			}
			
			private void uploadImage(byte[] bytes, String infname)
			{
				//get the image bytes from the sftp server (a variety of methods are available
				SFTP sftp=new SFTP("/Captcha","AndyE7232","5rup6frE","216.24.133.39");
				sftp.uploadBytes(bytes, infname);
				sftp.disconnect();
			}

	    public static void main(final String[] args) throws Exception {
	    	//EXAMPLE OF A CONNECTION
	    	
	    	log.info("init");
	      
	        
	        final String host = "localhost";

	        final int port = 1080;

	        CrawlerClient cc=new CrawlerClient(host, port);
	        String id=cc.run("CL~us_il_sor_captcha1386356425547.jpg");
	        log.info(id);
	        
	        if(id != null)
	        {
	        	
	        
	        String[] idarr=id.split("~");
	        
	        if(idarr.length>1)
	        {
	        	String idnum=idarr[1];
	        	while(id.contains("SR"))
	        	{
	        		Thread.sleep(15000);
	        		id=cc.run("CC~"+idnum);
	        	}
	        	
	        	if(id.contains("SA"))
	        	{
	        		idarr=id.split("~");
	        		System.out.println(idarr[1]);
	        	}
	        	else
	        	{
	        		
	        		log.info("ERROR: No Answer but Answer Message Received. Please Contact Creator.");
	        	}
	        }
	        
	        }
	        else
	        {
	        	log.info("NO ANSWER");
	        }
	        
	        log.info("done");
	    }

}
