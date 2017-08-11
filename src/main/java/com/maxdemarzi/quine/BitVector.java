package com.maxdemarzi.quine;

import java.util.ArrayList;
import java.util.List;

// wrapper for the BitSet class
public class BitVector {
    // member variables
    private List<Long> bitVectorList;
    private int mySize;

    public BitVector(int size) {
        mySize = size;
        int adjustedSize = ((size - 1) / 64) + 1;
        bitVectorList = new ArrayList<>();
        for (int i = 0; i < adjustedSize; i++) {
            long newLong = 0;
            bitVectorList.add(newLong);
        }
    }

    public BitVector(int size, List<Long> bitVector) {
        mySize = size;
        bitVectorList = bitVector;
    }

    public void verifySize(int index) {
        if ((index + 1) > mySize) {
            int oldAdjustedSize = ((mySize - 1) / 64) + 1;
            int newAdjustedSize = ((index) / 64) + 1;
            for (int i = oldAdjustedSize; i < newAdjustedSize; i++) {
                long newLong = 0;
                bitVectorList.add(newLong);
            }
            mySize = index + 1;
        }
    }

    public boolean isZero() {
        for (Long myLong : bitVectorList) {
            if (myLong != 0)
                return false;
        }
        return true;
    }

    public boolean exists(int index) {
        verifySize(index);
        int adjustedSize = (index / 64);
        long bitVectorChunk = bitVectorList.get(adjustedSize);
        long one = 1;
        bitVectorChunk = bitVectorChunk & (one << (index % 64));
        return bitVectorChunk != 0;
    }

    public void set(int index) {
        verifySize(index);
        int adjustedSize = (index / 64);
        long bitVectorChunk = bitVectorList.get(adjustedSize);
        long one = 1;
        bitVectorChunk = bitVectorChunk | (one << (index % 64));
        bitVectorList.set(adjustedSize, bitVectorChunk);
    }

    public void unset(int index) {
        verifySize(index);
        int adjustedSize = (index / 64);
        long bitVectorChunk = bitVectorList.get(adjustedSize);
        long one = 1;
        long mask = bitVectorChunk & (one << (index % 64));
        bitVectorChunk = bitVectorChunk & (~mask);
        bitVectorList.set(adjustedSize, bitVectorChunk);
    }

    public void unsetAll() {
        for (int i = 0; i < mySize; i++) {
            unset(i);
        }
    }

    public int findNeededImplicant() {
        for (int i = 0; i < mySize; i++) {
            if (exists(i)) {
                return i;
            }
        }
        return 0;
    }

    public int getSize() {
        return mySize;
    }

    public List<Long> getBitVectorList() {
        return bitVectorList;
    }

    public int getCardinality() {
        int cardinality = 0;
        for (int i = 0; i < bitVectorList.size(); i++) {
            cardinality += Long.bitCount(bitVectorList.get(i));
        }
        return cardinality;
    }

    public boolean equals(BitVector bitVector) {
        int iterationSize = bitVector.getSize();
        if (iterationSize < mySize) {
            iterationSize = mySize;
        }

        int adjustedIterationSize = (iterationSize / 64) + 1;

        for (int i = 0; i < adjustedIterationSize; i++) {
            long bitVectorChunk = 0;
            if (i < bitVector.getBitVectorList().size()) {
                bitVectorChunk = bitVector.getBitVectorList().get(i);
            }

            long myChunk = 0;
            if (i < bitVectorList.size()) {
                myChunk = bitVectorList.get(i);
            }

            if (bitVectorChunk != myChunk) return false;
        }

        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("[");

        int count = 0;
        for (Long l : bitVectorList) {
            if (count == 1)
                sb.append(",");
            count = 1;
            sb.append(Long.toHexString(l));
        }

        sb.append("]");

        return sb.toString();
    }

    public BitVector union(BitVector bitVector) {
        int iterationSize = bitVector.getSize();
        if (iterationSize < mySize) {
            iterationSize = mySize;
        }

        int adjustedIterationSize = (iterationSize / 64) + 1;

        List<Long> newBitVector = new ArrayList<Long>();
        for (int i = 0; i < adjustedIterationSize; i++) {
            long bitVectorChunk = 0;
            if (i < bitVector.getBitVectorList().size()) {
                bitVectorChunk = bitVector.getBitVectorList().get(i);
            }

            long myChunk = 0;
            if (i < bitVectorList.size()) {
                myChunk = bitVectorList.get(i);
            }

            long newBitVectorChunk = myChunk | bitVectorChunk;
            newBitVector.add(newBitVectorChunk);
        }
        return new BitVector(iterationSize, newBitVector);
    }

    public BitVector intersection(BitVector bitVector) {
        int iterationSize = bitVector.getSize();
        if (iterationSize < mySize) {
            iterationSize = mySize;
        }

        int adjustedIterationSize = (iterationSize / 64) + 1;

        List<Long> newBitVector = new ArrayList<Long>();
        for (int i = 0; i < adjustedIterationSize; i++) {
            long bitVectorChunk = 0;
            if (i < bitVector.getBitVectorList().size()) {
                bitVectorChunk = bitVector.getBitVectorList().get(i);
            }

            long myChunk = 0;
            if (i < bitVectorList.size()) {
                myChunk = bitVectorList.get(i);
            }

            long newBitVectorChunk = myChunk & bitVectorChunk;
            newBitVector.add(newBitVectorChunk);
        }
        return new BitVector(iterationSize, newBitVector);

    }

}
