/*
 * $Id$
 */

package org.ofbiz.core.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;
import java.util.*;
import java.io.*;

/**
 * <p><b>Title:</b> HttpRequestFileUpload - Receive a file upload through an HttpServletRequest
 * <p><b>Description:</b>
 * <p>Copyright (c) 2001 The Open For Business Project and repected authors.
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author    Dustin Caldwell
 * @version   1.0
 */
public class HttpRequestFileUpload {

    private int BUFFER_SIZE = 4096;
    private int WAIT_INTERVAL = 200; // in milliseconds
    private int MAX_WAITS = 20;
    private int waitCount = 0;
    private String savePath;
    private String filepath;
    private String filename;
    private String contentType;
    private String overrideFilename = null;
    private Dictionary fields;

    public String getOverrideFilename() {
        return overrideFilename;
    }

    public void setOverrideFilename(String ofName) {
        overrideFilename = ofName;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFieldValue(String fieldName) {
        if (fields == null || fieldName == null)
            return null;
        return (String) fields.get(fieldName);
    }

    private void setFilename(String s) {
        if (s == null)
            return;

        int pos = s.indexOf("filename=\"");
        if (pos != -1) {
            filepath = s.substring(pos + 10, s.length() - 1);
            // Windows browsers include the full path on the client
            // But Linux/Unix and Mac browsers only send the filename
            // test if this is from a Windows browser
            pos = filepath.lastIndexOf("\\");
            if (pos != -1)
                filename = filepath.substring(pos + 1);
            else
                filename = filepath;
        }
    }

    private void setContentType(String s) {
        if (s == null)
            return;

        int pos = s.indexOf(": ");
        if (pos != -1)
            contentType = s.substring(pos + 2, s.length());
    }

    public void doUpload(HttpServletRequest request) throws IOException {
        ServletInputStream in = request.getInputStream();
/*        System.out.println("Header:");
        Enumeration ee = request.getHeaderNames();
        while(ee.hasMoreElements()) {
            String ss = (String)ee.nextElement();
            System.out.println(ss + " = [" + request.getHeader(ss) + "]");
        }*/
        String reqLengthString = request.getHeader("content-length");
        System.out.println("expect " + reqLengthString + " bytes.");
        int requestLength = 0;
        try {
            requestLength = new Integer(reqLengthString).intValue();
        } catch (Exception e2) {
            e2.printStackTrace();
            return;
        }
        byte[] line = new byte[BUFFER_SIZE];

        int i = -1;
        i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
        requestLength -= i;
        if (i < 3)
            return;
        int boundaryLength = i - 2;

        String boundary = new String(line, 0, boundaryLength); //-2 discards the newline character
        System.out.println("boundary=[" + boundary + "] length is " + boundaryLength);
        fields = new Hashtable();

        while (requestLength > 0/*i != -1*/) {
            String newLine = "";
            if (i > -1)
                newLine = new String(line, 0, i);
            if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
                if (newLine.indexOf("filename=\"") != -1) {
                    setFilename(new String(line, 0, i - 2));
                    if (filename == null)
                        return;
                    //this is the file content
                    i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                    requestLength -= i;

                    setContentType(new String(line, 0, i - 2));

                    // blank line
                    i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                    requestLength -= i;
                    newLine = new String(line, 0, i);
                    String filenameToUse = filename;
                    if (overrideFilename != null) {
                        filenameToUse = overrideFilename;
                    }

                    // first line of actual file
                    i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                    requestLength -= i;
                    newLine = new String(line, 0, i);

                    byte[] lastTwoBytes = new byte[2];
                    if (i > 1) {
                        lastTwoBytes[0] = line[i - 2];
                        lastTwoBytes[1] = line[i - 1];
                    }
                    System.out.println("about to create a file:" + (savePath == null? "" : savePath) + filenameToUse);
                    FileOutputStream fos = new FileOutputStream((savePath == null? "" : savePath) + filenameToUse);
                    boolean bail = (new String(line, 0, i).startsWith(boundary));
                    boolean oneByteLine = (i == 1); // handle one-byte lines
                    while ((requestLength > 0/*i != -1*/) && !bail) {

                        // write the current buffer, except the last 2 bytes;
                        if (i > 1) {
                            fos.write(line, 0, i - 2);
                        }

                        oneByteLine = (i == 1); // we need to track on-byte lines differently

                        i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                        requestLength -= i;

                        // the problem is the last line of the file content
                        // contains the new line character.

                        // if the line just read was the last line, we're done.
                        // if not, we must write the last 2 bytes of the previous buffer
                        // just assume that a one-byte line isn't the last line

                        if (requestLength < 1) {
                            bail = true;
                        } else if (oneByteLine) {
                            fos.write(lastTwoBytes, 0, 1); // we only saved one byte
                        } else {
                            fos.write(lastTwoBytes, 0, 2);
                        }

                        if (i > 1) {
                            // save the last 2 bytes of the buffer
                            lastTwoBytes[0] = line[i - 2];
                            lastTwoBytes[1] = line[i - 1];
                        } else {
                            lastTwoBytes[0] = line[0]; // only save one byte
                        }
                    }
                    fos.flush();
                    fos.close();
                } else {
                    //this is a field
                    // get the field name
                    int pos = newLine.indexOf("name=\"");
                    String fieldName = newLine.substring(pos + 6, newLine.length() - 3);
                    //System.out.println("fieldName:" + fieldName);
                    // blank line
                    i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                    requestLength -= i;
                    i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                    requestLength -= i;
                    newLine = new String(line, 0, i);
                    StringBuffer fieldValue = new StringBuffer(BUFFER_SIZE);
                    while (requestLength > 0/*i != -1*/ && !newLine.startsWith(boundary)) {
                        // The last line of the field
                        // contains the new line character.
                        // So, we need to check if the current line is
                        // the last line.
                        i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
                        requestLength -= i;
                        if ((i == boundaryLength + 2 || i == boundaryLength + 4) // + 4 is eof
                                && (new String(line, 0, i).startsWith(boundary)))
                            fieldValue.append(newLine.substring(0, newLine.length() - 2));
                        else
                            fieldValue.append(newLine);
                        newLine = new String(line, 0, i);
                    }
                    //System.out.println("fieldValue:" + fieldValue.toString());
                    fields.put(fieldName, fieldValue.toString());
                }
            }
            i = waitingReadLine(in, line, 0, BUFFER_SIZE, requestLength);
            if (i > -1)
                requestLength -= i;

        } // end while
    }

    // reads a line, waiting if there is nothing available and reqLen > 0
    private int waitingReadLine(ServletInputStream in, byte[] buf, int off, int len, int reqLen) throws IOException {
        int i = -1;
        while (((i = in.readLine(buf, off, len)) == -1) && (reqLen > 0)) {
            System.out.print("waiting");
            if (waitCount > MAX_WAITS) {
                System.out.println("waited " + waitCount + " times, bailing out while still expecting " +
                                   reqLen + " bytes.");
                throw new IOException("waited " + waitCount + " times, bailing out while still expecting " +
                                      reqLen + " bytes.");
            }
            waitCount++;
            long endMS = new Date().getTime() + WAIT_INTERVAL;
            while (endMS > (new Date().getTime())) {
                try {
                    wait(WAIT_INTERVAL);
                } catch (Exception e3) {
                    System.out.print(".");
                }
            }
            System.out.println((new Date().getTime() - endMS) + " ms");
        }
        return i;
    }

}
