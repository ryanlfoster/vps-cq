package com.vps.dispatcher.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;

public class NonCommittingSlingHttpResponseWrapper extends SlingHttpServletResponseWrapper {
    private BufferedServletOutputStream bufferedServletOut = new BufferedServletOutputStream( );

    private PrintWriter printWriter = null;
    private ServletOutputStream outputStream = null;

	// This is the default status unless it gets explicitly set
	private int observableStatus = 200;
	
	public NonCommittingSlingHttpResponseWrapper(SlingHttpServletResponse wrappedResponse) {
		super(wrappedResponse);
	}

	// New capabilities of this class
	
	/**
	 * Answer the status set against the wrapped response
	 * @return
	 */
	public int getStatus(){
		return observableStatus;
	}

	/**
	 * Answer the current buffer
	 * @return
	 */
	public byte[] getBuffer( ) {
        return this.bufferedServletOut.getBuffer( );
    }
	
	/**
	 * Write the buffered response to the real response
	 */
	public void writeToRealResponse() throws IOException {
		this.flushBuffer();
		this.getSlingResponse().getOutputStream().write(this.getBuffer());
		this.getSlingResponse().flushBuffer();
	}
	
	// Overridden methods 
	@Override
	public void setStatus(int sc) {
		this.observableStatus = sc;
		
		super.setStatus(sc);
	}


	@Override
	public void setStatus(int sc, String sm) {
		this.observableStatus = sc;
		super.setStatus(sc, sm);
	}
	
	@Override
	public void sendError(int sc, String msg) throws IOException {
		this.observableStatus = sc;
		
		super.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		this.observableStatus = sc;
		
		super.sendError(sc);
	}

	@Override
    public PrintWriter getWriter( ) throws IOException {
        if (this.outputStream != null) {
            throw new IllegalStateException(
                    "The Servlet API forbids calling getWriter( ) after getOutputStream( ) has been called");
        }

        if (this.printWriter == null) {
            this.printWriter = new PrintWriter(this.bufferedServletOut);
        }
        return this.printWriter;
    }
	
	@Override
    public ServletOutputStream getOutputStream( ) throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException(
                "The Servlet API forbids calling getOutputStream( ) after getWriter( ) has been called");
        }

        if (this.outputStream == null) {
            this.outputStream = this.bufferedServletOut;
        }
        return this.outputStream;
    }

	@Override
    public void flushBuffer( ) throws IOException {
        if (this.outputStream != null) {
            this.outputStream.flush( );
        } else if (this.printWriter != null) {
            this.printWriter.flush( );
        }
    }

	@Override
    public int getBufferSize( ) {
        return this.bufferedServletOut.getBuffer( ).length;
    }
	
	@Override
    public void reset( ) {
        this.bufferedServletOut.reset( );
    }

	@Override
    public void resetBuffer( ) {
        this.bufferedServletOut.reset( );
    }

	@Override
    public void setBufferSize(int size) {
        this.bufferedServletOut.setBufferSize(size);
    }
	
}
