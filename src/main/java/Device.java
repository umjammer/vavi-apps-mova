/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import vavi.io.IODevice;
import vavi.io.IODeviceInputStream;
import vavi.io.IODeviceOutputStream;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * Device
 * 
 * @author ARAI, Shunichi
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040325 nsano initial version <br>
 */
public abstract class Device {

    /** �����ۗ� ���M���̉����ۗ��y�щ����ۗ������ɗ��p */
    public static final int COMMAND_SUSPEND = 0x82;

    /** �������_�C�����������ݗv�� �������_�C�����ԍ� nm �ɏ������݂܂� */
    public static final int COMMAND_WRITEMEMORY = 0x83; // + 0x0n + 0x0m 

    /**
     * �_�C��������
     * �v���Z�b�g���M���̃_�C�����M���̒���y�у������_�C�����������ݗv������
     * �_�C�����M���̒���ɑ��o���܂�
     */
    public static final int COMMAND_FINISHDIAL = 0x86;

    /** �������_�C�����ǂݏo���v�� �������_�C�����ԍ� nm ��ǂݏo���܂� */
    public static final int COMMAND_READMEMORY = 0x87; // + 0x0n + 0x0m 

    /** �_�C�������b�N�v�� �_�C�������b�N��������Ƃ��Ɏg�p���܂� */
    public static final int COMMAND_LOCKDIAL = 0x8a;

    /** �t�b�L���O �ʘb���̃t�b�L���O���Ɏg�p���܂� */
    public static final int COMMAND_HOOK = 0x8b;

    /** �l�ԍ��\���v�� ���̃P�[�^�C�̔ԍ���ǂݏo���Ƃ��Ɏg�p���܂� */
    public static final int COMMAND_GETNUMBER = 0x8e;

    /** �_�C���� �ԍ�k(��1)���_�C�������܂� */
    public static final int COMMAND_DIAL = 0x90; // + k

    /** �t�b�N�X�C�b�`(1) �I���N���[�h���ŃI���t�b�N(�I��)���܂� */
    public static final int COMMAND_STOPTONHOOK = 0xa4;

    /** �t�b�N�X�C�b�`(2) �I���N���[�h���ŃI�t�t�b�N(�J�n)���܂� */
    public static final int COMMAND_STARTINHOOK = 0xa5;

    /** �t�b�N�X�C�b�`(3) �I�t�N���[�h���ŃI���t�b�N(�I��)���܂� */
    public static final int COMMAND_STOPOFFHOOK = 0xa6;

    /** �t�b�N�X�C�b�`(4) �I�t�N���[�h���ŃI�t�t�b�N(�J�n)���܂� */
    public static final int COMMAND_STARTOFFHOOK = 0xa7;

    /**
     * ���b�~���[�g OFF ��Ԓʒm
     * PC �����~���[�gOFF ��Ԃł��邱�Ƃ�ʒm���܂�
     */
    public static final int COMMAND_MUTEOFF = 0xc0;

    /**
     * ���b�~���[�g ON ��Ԓʒm
     * PC �����~���[�gON ��Ԃł��邱�Ƃ�ʒm���܂�
     */
    public static final int COMMAND_MUTEON = 0xc1;

    /**
     * ���M�T�C�����g���[�h OFF ��Ԓʒm
     * PC �������M���̌ďo�����[�h�� OFF ��Ԃł��邱�Ƃ�ʒm���܂�
     */
    public static final int COMMAND_SILENTOFF = 0xc2;

    /**
     * ���M�T�C�����g���[�h ON ��Ԓʒm
     * PC �������M���̌ďo�����[�h�� ON ��Ԃł��邱�Ƃ�ʒm���܂�
     */
    public static final int COMMAND_SILENTON = 0xc3;

    /**
     * �񓮍�M��
     * �񓮍�M���̃w�b�_ (0xD0) �ɑ����đ��M�����M����C�ӂ̗p�r��
     * �g�p���邱�Ƃ��\
     */
    public static final int COMMAND_PROHIBIT = 0xd0; // + 0x0n (+ 0x0m + ...)

    /** ���M�]���v��(�g�[�L����) ���M�]���Ɏg�p���܂� */
    public static final int COMMAND_E0 = 0xe0;

    /** ���M�]���v��(�g�[�L����) ���M�]���Ɏg�p���܂� */
    public static final int COMMAND_E1 = 0xe1;

    /** ����ԓd�b�v�� ���M���̗���ԓd�b�Ɏg�p���܂� */
    public static final int COMMAND_E2 = 0xe2;

    /** �ʐM���]���v�� �ʐM���̑���𑼂̑���ɓ]�����鎞�Ɏg�p���܂� */
    public static final int COMMAND_E3 = 0xe3;

    /** �O�Ғʘb�v��(�ؑփ��[�h) �ʐM���̔��M���s�Ȃ����Ɏg�p���܂� */
    public static final int COMMAND_E4 = 0xe4;

    /** �O�Ғʘb�v��(�~�L�V���O���[�h) �ʐM���̔��M���s�Ȃ����Ɏg�p���܂� */
    public static final int COMMAND_E5 = 0xe5;

    /**
     * �ۗ��Đؒf�v��
     * �O�Ғʘb�܂��̓R�[���E�F�C�e�B���O���ɕۗ��Ƃ��Ă���l�Ƃ̒ʘb��
     * �I������Ƃ��Ɏg�p���܂�
     */
    public static final int COMMAND_E7 = 0xe7;

    /** ��d�b�T�[�r�X�v��(G3 FAX OFF�v��) G3 FAX �� OFF ���� */
    public static final int COMMAND_E8_00 = 0xe8; // + 0x00

    /** ��d�b�T�[�r�X�v��(MNP���f��OFF�v��) MNP ���f���� OFF ���� */
    public static final int COMMAND_E8_01 = 0xe8; // + 0x01

    /** ��d�b�T�[�r�X�v��(G3 FAX ON�v��) G3 FAX �� ON ���� */
    public static final int COMMAND_E8_08 = 0xe8; // + 0x08

    /** ��d�b�T�[�r�X�v��(MNP���f��ON�v��) MNP ���f���� ON ���� */
    public static final int COMMAND_E8_09 = 0xe8; // + 0x09

    /** ���M���ۗv�� ���M�����ۂ���Ƃ��Ɏg�p���܂�(���M���ɑ��o) */
    public static final int COMMAND_E9 = 0xe9;

    /** ���ԍ��\������ ���ԍ���\�����邱�Ƃ��\�ɂ��܂� */
    public static final int COMMAND_EA = 0xea;

    /** ���ԍ��\���֎~ ���ԍ���\�����邱�Ƃ�s�\�ɂ��܂� */
    public static final int COMMAND_EB = 0xeb;

    /** VOX �w��Ȃ� VOX �Ȃ� */
    public static final int COMMAND_6A = 0x6a;

    /** VOX �w�肠�� VOX ���� */
    public static final int COMMAND_6B = 0x6b;

    //----

    /** */
    public static final int TK_NW = 0x43;

    /** */
    public static final int TK_UP = 0x11;

    /** */
    public static final int TK_NE = 0x44;

    /** */
    public static final int TK_LEFT = 0x13;

    /** */
    public static final int TK_CENTER = 0x62;

    /** */
    public static final int TK_RIGHT = 0x14;

    /** TODO */
    public static final int TK_SW = 0x31;

    /** */
    public static final int TK_DOWN = 0x12;

    /** TODO */
    public static final int TK_SE = 0x42;

    /** */
    public static final int TK_ONHOOK = 0x2e;

    /** */
    public static final int TK_CLEAR = 0x63;

    /** */
    public static final int TK_OFFHOOK = 0x2d;

    /** */
    public static final int TK_NUMPAD1 = 0x21;

    /** */
    public static final int TK_NUMPAD2 = 0x22;

    /** */
    public static final int TK_NUMPAD3 = 0x23;

    /** */
    public static final int TK_NUMPAD4 = 0x24;

    /** */
    public static final int TK_NUMPAD5 = 0x25;

    /** */
    public static final int TK_NUMPAD6 = 0x26;

    /** */
    public static final int TK_NUMPAD7 = 0x27;

    /** */
    public static final int TK_NUMPAD8 = 0x28;

    /** */
    public static final int TK_NUMPAD9 = 0x29;

    /** */
    public static final int TK_ASTERISK = 0x2c;

    /** */
    public static final int TK_NUMPAD0 = 0x2a;

    /** */
    public static final int TK_SHARP = 0x2b;

    /** TODO */
    public static final int TK_I1 = 0x61;

    /** TODO */
    public static final int TK_I4 = 0x64;

    /** TODO */
    public static final int TK_I5 = 0x65;

    /** side A */
    public static final int TK_SIDEA = 0x45;

    /** side B */
    public static final int TK_SIDEB = 0x46;

    /** back */
    public static final int TK_BACK = 0x47;

    //----

    /** */
    private static final int[] table = new int[256];

    /** */
    static {
        for (int i = 0; i < 256; i++) {
            table[i] = 0;
        }

        // latin capital letters
        for (int i = 0x0041; i <= 0x005a; i++) {
            int c = i - 0x0041;

            if (c < 10) {
                table[i] = c + 0x16;
            } else if (c < 20) {
                table[i] = (c + 0x26) - 10;
            } else {
                table[i] = (c + 0x36) - 20;
            }
        }

        // latin small letters
        for (int i = 0x0061; i <= 0x007a; i++) {
            int c = i - 0x0061;

            if (c < 10) {
                table[i] = c + 0x46;
            } else if (c < 20) {
                table[i] = (c + 0x56) - 10;
            } else {
                table[i] = (c + 0x66) - 20;
            }
        }

        // half-width digits
        table[0x30] = 0xf5;

        for (int i = 0x0031; i <= 0x0039; i++) {
            int c = i - 0x0031;

            if (c < 5) {
                table[i] = c + 0xe1;
            } else {
                table[i] = (c + 0xf1) - 5;
            }
        }

        // half-width marks
        table[0x20] = 0x9e; //   Space
        table[0x21] = 0xd4; // ! Exclamation
        table[0x22] = 0x86; // " Quotation
        table[0x23] = 0xf6; // # Number Sign
        table[0x24] = 0x87; // $
        table[0x25] = 0x88; // %
        table[0x26] = 0x7f; // &
        table[0x27] = 0x7d; // '
        table[0x28] = 0x82; // (
        table[0x29] = 0x84; // )
        table[0x2A] = 0xf7; // *
        table[0x2B] = 0x89; // +
        table[0x2C] = 0x79; // ,
        table[0x2D] = 0x7a; // -
        table[0x2E] = 0x77; // .
        table[0x2F] = 0x78; // /
        table[0x3a] = 0x7c; // :
        table[0x3b] = 0x8a; // ;
        table[0x3c] = 0x8b; // <
        table[0x3d] = 0x8c; // =
        table[0x3e] = 0x8d; // >
        table[0x3f] = 0xd5; // ?
        table[0x40] = 0x76; // @
        table[0x5b] = 0x8f; // [
        table[0x5c] = 0x8e; // \
        table[0x5d] = 0x9a; // ]
        table[0x5e] = 0x97; // ^
        table[0x5f] = 0x7b; // _
        table[0x60] = 0x9f; // `
        table[0x7b] = 0x98; // {
        table[0x7c] = 0x99; // |
        table[0x7d] = 0x9a; // }
        table[0x7e] = 0x9d; // ~
    }

    //----

    /** */
    protected IODevice device;

    /** */
    protected InputStream is;

    /** */
    protected OutputStream os;

    /** */
    public Device(IODevice device) {
        this.device = device;

        this.is = new IODeviceInputStream(device);
        this.os = new IODeviceOutputStream(device);
    }

    /** */
    public int getNumericCode(int i) {
        if ((i < 0) || (i > 9)) {
            throw new IllegalArgumentException("out of range: " + i);
        }

        if (i == 0) {
            i = 10;
        }

        i = i | 0x20;

        return i;
    }

    /** */
    public int getFunctionCode(int i) {
        if ((i < 1) || (i > 30)) {
            throw new IllegalArgumentException("out of range: " + i);
        }

        i += 0x30;

        return i;
    }

    /** */
    public void writeCommand(int tableType, int i) throws IOException {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xf2; // Ext. Terminal Control Signal Header
        buf[1] = 0x02; // Ext. Terminal Control Signal Input Request Header
        buf[2] = 0x01; // Ext. Terminal Control Signal Input Request Header
        buf[3] = 0x00; // Identify code
        buf[4] = 0x00; // Identify code
        buf[5] = (byte) (tableType & 15); // Table type
        buf[6] = (byte) ((i >> 4) & 15); // Control code
        buf[7] = (byte) (i & 15); // Control code
        os.write(buf);
    }

    /** */
    private void write30(int c) throws IOException {
        if ((c >= 0x3041) && (c <= 0x304a)) {
            // hiragana
            // aiueo / small aiueo
            int d = c - 0x3041;

            if ((d & 1) == 0) {
                // small
                writeCommand(2, 0xb1 + (d / 2));
            } else {
                // large
                writeCommand(2, 0x11 + (d / 2));
            }
        } else if ((c >= 0x304b) && (c <= 0x3069)) {
            // [kg][aiueo] / [sz][aiueo]
            // [td][aiueo] + small tu
            int d = c - 0x304b;

            if (d < 10) {
                // [kg] [sz]
                writeCommand(2, 0x21 + (d / 2));
            } else if (d < 20) {
                d -= 10;
                writeCommand(2, 0x31 + (d / 2));
            } else {
                // [td]
                d -= 20;

                if (d == 4) {
                    writeCommand(2, 0xc1);
                } else {
                    if (d > 4) {
                        d--;
                    }

                    writeCommand(2, 0x41 + (d / 2));
                }
            }

            if ((d & 1) == 1) {
                // [gzd]
                writeCommand(2, 0xa4);
            }
        } else if ((c >= 0x306a) && (c <= 0x306e)) {
            // naninuneno
            int d = c - 0x306a;
            writeCommand(2, 0x51 + d);
        } else if ((c >= 0x306f) && (c <= 0x307d)) {
            // [hpb][aiueo]
            int d = c - 0x306f;
            writeCommand(2, 0x61 + (d / 3));

            if ((d % 3) == 1) {
                // [b]
                writeCommand(2, 0xa4);
            } else if ((d % 3) == 2) {
                // [p]
                writeCommand(2, 0xa5);
            }
        } else if ((c >= 0x307e) && (c <= 0x3082)) {
            // mamimumemo
            int d = c - 0x307e;
            writeCommand(2, 0x71 + d);
        } else if ((c >= 0x3083) && (c <= 0x3088)) {
            // yayuyo / small yayuyo
            int d = c - 0x3083;

            if ((d & 1) == 0) {
                // small
                writeCommand(2, 0xc2 + (d / 2));
            } else {
                // large
                writeCommand(2, (0x81 + d) - 1);
            }
        } else if ((c >= 0x3089) && (c <= 0x308d)) {
            // rarirurero
            int d = c - 0x3089;
            writeCommand(2, 0x91 + d);
        } else if (c == 0x308f) {
            // wa/wo/n
            writeCommand(2, 0xa1);
        } else if (c == 0x3092) {
            writeCommand(2, 0xa2);
        } else if (c == 0x3093) {
            writeCommand(2, 0xa3);
        } else if (c == 0x3001) {
            // ideographic comma/full stop, katakana middle dot
            // katakana-hiragana prolonged sound mark
            writeCommand(2, 0xd1);
        } else if (c == 0x3002) {
            writeCommand(2, 0xd2);
        } else if (c == 0x30fb) {
            writeCommand(2, 0xd3);
        } else if (c == 0x30fc) {
            writeCommand(2, 0xc5);
        }
    }

    /** */
    public void write(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c < 0x0100) {
                // latin letters/digits/marks
                writeCommand(1, table[c]);
            } else if ((c >= 0x3000) && (c < 0x3100)) {
                // full hiragana
                write30(c);
                writeCommand(3, TK_CENTER);
            }
        }
    }

    /* */
    public abstract String getDeviceId() throws IOException;

    /** */
    private static Properties names = new Properties();

    /** */
    private static BidiMap commands = new TreeBidiMap();

    /** */
    int getKeyCode(String name) {
        String keyCode = (String) commands.getKey(name);
//System.err.println("name, keyCode: " + name + ", " + keyCode);
        return Integer.parseInt(keyCode, 16);
    }

    /** */
    String getKeyName(int keyCode) {
        return (String) commands.get(StringUtil.toHex2(keyCode));
    }

    /** */
    String getDisplayName(String keyName) {
        return names.getProperty(keyName);
    }

    /** */
    static {
        try {
            Properties keys = new Properties();
            final String path1 = "key.properties";
            keys.load(DeviceUI.class.getResourceAsStream(path1));

            Iterator<?> i = keys.keySet().iterator();
            while (i.hasNext()) {
                Object key = i.next();
                Object value = keys.get(key);
//System.err.println("com: " + key + ", " + value);
                commands.put(key, value);
            }

            final String path2 = "name.properties";
            names.load(DeviceUI.class.getResourceAsStream(path2));
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }
}

/* */
