package zk.obd.ble;

public class Signal {
    private byte[] canTX=new byte[10];
    private byte[] canRX= new byte[72];//18*4: TML id in 8 2*CRC,0a,0d
    private String signaltype="int";//0-int,1-ASC
    private int startbyte;
    private int startbit;
    private int length;
    private String unit;
    private double factor=1;
    private double offset=0;
    private double signalvalue;
    private String VIN;

    public double getSignalvalue() {
        long num = 0;
        for (int ix = 6; ix < 14; ++ix) {
            num <<= 8;
            num |= (canRX[ix] & 0xff);
        }
        num<<=((startbyte-1)*8+startbit);
        num>>=(64-length);
        signalvalue=(double)num*factor+offset;
        return signalvalue;
    }

    public String getVIN() {
        String RxVIN = new String(canRX);
        VIN=RxVIN.substring(7, 14)+RxVIN.substring(7+18, 14+18)+RxVIN.substring(7+18*2, 14+18*2)+RxVIN.substring(7+18*3, 14+18*3);
        return VIN;
    }

    public byte[] getCanTX() {
        return canTX;
    }

    public void setCanTX(byte[] canTX) {
        this.canTX = canTX;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public byte[] getCanRX() {
        return canRX;
    }

    public void setCanRX(int j,byte[] canRX) {
        if (j<1) j=1;
        System.arraycopy(canRX, 0, this.canRX, (j-1)*18, 18);
    }

    public String getSignaltype() {
        return signaltype;
    }

    public void setSignaltype(String signaltype) {
        this.signaltype = signaltype;
    }

    public int getStartbyte() {
        return startbyte;
    }

    public void setStartbyte(int startbyte) {
        this.startbyte = startbyte;
    }

    public int getStartbit() {
        return startbit;
    }

    public void setStartbit(int startbit) {
        this.startbit = startbit;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

}
