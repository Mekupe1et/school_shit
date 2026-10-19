package com.example.pizzaorder;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Order implements Parcelable {
    private int sum;
    private String size;
    private String crust;
    public Order(int sum, String size, String crust) {
        this.sum = sum;
        this.size = size;
        this.crust = crust;
    }

    protected Order(Parcel in) {
        sum = in.readInt();
        size = in.readString();
        crust = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(sum);
        dest.writeString(size);
        dest.writeString(crust);
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCrust() {
        return crust;
    }

    public void setCrust(String crust) {
        this.crust = crust;
    }
}