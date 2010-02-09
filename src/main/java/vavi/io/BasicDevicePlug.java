/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * BasicDevice.
 * �T�u�N���X�͕K�� (Ljava/lang/String;) �̃V�O�l�`�������R���X�g���N�^��
 * �����Ȃ���΂Ȃ�܂���B
 *		
 * @see		#newInstance(String,String)
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version	0.00	040309	nsano	initial version <br>
 */
public abstract class BasicDevicePlug extends AbstractDevicePlug {

    /** */
    public static final AbstractDevicePlug newInstance(String className, String name)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {

        return (AbstractDevicePlug) newInstanceInternal(className, name);
    }

    /** */
    private static Object newInstanceInternal(String className, String name)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {
//Debug.println(className + ": " + name);
        Class<?> clazz = Class.forName(className);
        Constructor<?> c = clazz.getConstructor(String.class);
        return c.newInstance(name);
    }

    /** ���ʎq(�e�f�o�C�X�̃v���p�e�B�t�@�C���Ƀ��X�g���ꂽ���̂��w��) */
    protected String name;

    /**
     * @param	name	�v���p�e�B�t�@�C���̂ǂ� IODevice ���g�p���邩���w��
     */
    public BasicDevicePlug(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------

    /** ���̃f�o�C�X�� IO �f�o�C�X�N���X���擾���܂��B */
    protected abstract String getIODeviceClass();

    /**
     * ���̃f�o�C�X�� IO �f�o�C�X�N���X�̎��ʎq���擾���܂��B
     * (�V���A���|�[�g���� IP �̃|�[�g�ԍ����w�肳��d���I�[�v��������܂�)
     */
    protected abstract String getIODeviceName();

    /** */
    protected IODeviceInputStream is;

    /** */
    protected IODeviceOutputStream os;

    /** IO �f�o�C�X�̎��ʎq�AIO �f�o�C�X�̃y�A */
    private Map<String,IODevice> ioDevices = new HashMap<String,IODevice>();

    /**
     * IO �f�o�C�X���擾���܂��B
     * �����ŃC���X�^���X������� IODevice �̎����N���X�͕K��
     * (Ljava/lang/String;) �̃V�O�l�`�������R���X�g���N�^��
     * �����Ȃ���΂Ȃ�܂���B
     */
    private IODevice getIODevice()
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {

        String className = getIODeviceClass();
        String name = getIODeviceName();

        if (ioDevices.containsKey(name)) {
            return ioDevices.get(name);
        } else {
            IODevice ioDevice = (IODevice) newInstanceInternal(className, name);
            ioDevices.put(name, ioDevice);
Debug.println("name: " + name + ": " + className);
            return ioDevice;
        }
    }

    /**
     * ���̓X�g���[�����I�[�v�����Ă��Ȃ���΃I�[�v�����܂��B
     * �R���X�g���N�^�ŃI�[�v�����Ȃ��͕̂Е��݂̂̃f�o�C�X���݂邽��
     */
    protected void makeSureInputStreamOpened() {
        if (this.is != null) {
            return;
        }

        try {
            IODevice ioDevice = getIODevice();
            this.is = new IODeviceInputStream(ioDevice);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
Debug.printStackTrace(t);
            throw (RuntimeException) new IllegalStateException().initCause(t);
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    protected Runnable getInputThread() {
        makeSureInputStreamOpened();

        return new Runnable() {
            public void run() {
Debug.println("IN[" + getIODeviceName() + "]: thread started");

                while (loop) {
                    int c = -1;
                    try {
                        c = is.read();
                        if (c == -1) {
//Debug.println("IN[" + getIODeviceName() + "]: -1 received");
                        } else {
Debug.println("IN[" + getIODeviceName() + "]: " + StringUtil.toHex2(c));
                            fireEventHappened(
                                new GenericEvent(this,
                                                 "data",
                                                 new Integer(c)));
                        }
                    } catch (IllegalArgumentException e) {
//Debug.printStackTrace(e);
System.err.println("IN[" + getIODeviceName() + "]> " + (char) c);
                    } catch (java.net.SocketException e) {
Debug.println(e.getMessage());
                    } catch (IOException e) {
Debug.printStackTrace(e);
                    }
                }

Debug.println("IN[" + getIODeviceName() + "]: thread stopped");
            }
        };
    }

    /**
     * �o�̓X�g���[�����I�[�v�����Ă��Ȃ���΃I�[�v�����܂��B
     * �R���X�g���N�^�ŃI�[�v�����Ȃ��͕̂Е��݂̂̃f�o�C�X���݂邽��
     */
    protected void makeSureOutputStreamOpened() {
        if (this.os != null) {
            return;
        }

        try {
            IODevice ioDevice = getIODevice();
            this.os = new IODeviceOutputStream(ioDevice);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
Debug.printStackTrace(t);
            throw (RuntimeException) new IllegalStateException().initCause(t);
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    protected GenericListener getOutputGenericListener() {
        makeSureOutputStreamOpened();

        // TODO check multiple instantiation
        return new GenericListener() {
            public void eventHappened(GenericEvent ev) {
                try {
                    int c = ((Integer) ev.getArguments()[0]).intValue();
//Debug.println("OUT[" + getIODeviceName() + "]: " + StringUtil.toHex2(c));
                    os.write(c);
//                  os.flush();
                } catch (java.net.SocketException e) {
Debug.println(e.getMessage());
                } catch (Exception e) {
Debug.printStackTrace(e);
                }
            }
        };
    }
}

/* */
