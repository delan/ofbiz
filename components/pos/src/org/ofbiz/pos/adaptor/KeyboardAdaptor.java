/*
 * $Id: KeyboardAdaptor.java,v 1.4 2004/08/15 22:43:04 ajzeneski Exp $
 *
 * Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.pos.adaptor;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;

import net.xoetrope.xui.XPage;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.collections.OrderedMap;
import org.ofbiz.pos.component.Input;


/**
 * KeyboardAdaptor - Handles reading keyboard input
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.4 $
 * @since      3.2
 */
public class KeyboardAdaptor {

    public static final String module = KeyboardAdaptor.class.getName();
    public static final int KEYBOARD_DATA = 100;
    public static final int SCANNER_DATA = 101;
    public static final int MSR_DATA = 102;
    public static final int ALL_DATA = 999;

    protected static List loadedComponents = new LinkedList();
    protected static Map receivers = new OrderedMap();
    protected static KeyboardAdaptor adaptor = null;
    protected static boolean running = true;

    protected KeyboardListener listener = null;

    public static KeyboardAdaptor getInstance(KeyboardReceiver receiver, int dataType) {
        return getInstance(receiver, dataType, null);
    }

    public static KeyboardAdaptor getInstance(KeyboardReceiver receiver, int dataType, Component[] coms) {
        if (adaptor == null) {
            synchronized(KeyboardAdaptor.class) {
                if (adaptor == null) {
                    adaptor = new KeyboardAdaptor();
                }
            }
        }

        // add the new components to listen on
        if (adaptor != null && coms != null) {
            adaptor.addComponents(coms);
        }

        receivers.put(receiver, new Integer(dataType));
        return adaptor;
    }

    public static void stop() {
        running = false;
    }

    private KeyboardAdaptor() {
        this.listener = new KeyboardListener();
        this.listener.setDaemon(false);
        this.listener.setName(listener.toString());
        this.listener.start();
        KeyboardAdaptor.adaptor = this;
    }

    private void addComponents(Component[] coms) {
        listener.reader.configureComponents(coms);
    }

    private class KeyboardListener extends Thread {

        private static final long MAX_WAIT = 200;
        private List keyCodeData = new LinkedList();
        private List keyCharData = new LinkedList();
        private long lastKey = -1;
        private KeyReader reader = null;

        public KeyboardListener() {
            this.reader = new KeyReader(this);
        }

        private void receiveKey(int keycode, char keychar) {
            lastKey = System.currentTimeMillis();
            keyCharData.add(new Character(keychar));
            keyCodeData.add(new Integer(keycode));
        }

        private int checkDataType(char[] chars) {            
            // test for scanner data
            if (((int) chars[0]) == 2 && ((int) chars[chars.length - 1]) == 10) {
                return SCANNER_DATA;
            // test for MSR data
            } else if (((int) chars[0]) == 37 && ((int) chars[chars.length - 1]) == 10) {
                return MSR_DATA;
            } else {
            // otherwise it's keyboard data
                return KEYBOARD_DATA;
            }
        }

        protected synchronized void sendData() {
            if (KeyboardAdaptor.receivers.size() > 0) {
                if (keyCharData.size() > 0) {
                    char[] chars = new char[keyCharData.size()];
                    int[] codes = new int[keyCodeData.size()];
                    for (int i = 0; i < keyCodeData.size(); i++) {
                        Character ch = (Character) keyCharData.get(i);
                        Integer itg = (Integer) keyCodeData.get(i);
                        codes[i] = itg.intValue();
                        chars[i] = ch.charValue();
                    }

                    Iterator ri = KeyboardAdaptor.receivers.keySet().iterator();
                    while (ri.hasNext()) {
                        KeyboardReceiver receiver = (KeyboardReceiver) ri.next();
                        int receiverType = ((Integer) receivers.get(receiver)).intValue();
                        int thisDataType = this.checkDataType(chars);
                        if (receiverType == ALL_DATA || receiverType == thisDataType) {
                            receiver.receiveData(codes, chars);
                        }
                    }

                    keyCharData = new LinkedList();
                    keyCodeData = new LinkedList();
                    lastKey = -1;
                }
            } else {
                Debug.logWarning("No receivers configured for key input", module);
            }
        }

        public void run() {            
            while (running) {
                long now = System.currentTimeMillis();
                if ((now - lastKey) >= MAX_WAIT) {
                    this.sendData();
                }

                if (!running) {
                    break;
                } else {
                    try {
                        Thread.sleep(MAX_WAIT);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    class KeyReader implements KeyListener {

        private KeyboardListener k;

        public KeyReader(KeyboardListener k) {
            this.k = k;
        }

        private void configureComponents(Component[] coms) {
            for (int i = 0; i < coms.length; i++) {
                if (!loadedComponents.contains(coms[i])) {
                    coms[i].addKeyListener(this);
                }
                if (coms[i] instanceof Container) {
                    Component[] nextComs = ((Container) coms[i]).getComponents();
                    configureComponents(nextComs);
                }
            }
        }

        public void keyTyped(KeyEvent e) {
            //Debug.log("Key Typed : " + e.getKeyChar(), module);
            char keyChar = e.getKeyChar();
            int keyCode = e.getKeyCode();
            k.receiveKey(keyCode, keyChar);
        }

        public void keyPressed(KeyEvent e) {            
            //Debug.log("Key Pressed : " + e.getKeyCode(), module);
        }

        public void keyReleased(KeyEvent e) {
            //Debug.log("Key Released : " + e.getKeyCode(), module);
        }
    }
}
