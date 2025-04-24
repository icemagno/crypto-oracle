package br.com.cmabreu.util;

import java.io.Serializable;
import java.math.BigInteger;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

public class Wallet implements Serializable  {
	private static final long serialVersionUID = 1L;
	private String address;
	private ECKeyPair ecKeyPair;
	private Bip32ECKeyPair bip32ECKeyPair;
	private Credentials credentials;
	private BigInteger privateKey;
	private BigInteger publicKey;
	private String mnemonic;
	private String fileName;
	
	public Wallet(String address, ECKeyPair ecKeyPair, Bip32ECKeyPair bip32ECKeyPair, String mnemonic, String fileName ) {
		this.bip32ECKeyPair = bip32ECKeyPair;
		this.fileName = fileName;
		this.ecKeyPair = ecKeyPair;
		this.address = address;
        this.publicKey = bip32ECKeyPair.getPublicKey();
        this.privateKey = bip32ECKeyPair.getPrivateKey();	
		this.mnemonic = mnemonic;
        this.credentials = Credentials.create(bip32ECKeyPair);
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public String getFileName() {
		return fileName;
	}
	
	public String getAddress() {
		return address;
	}
	
	public Bip32ECKeyPair getBip32ECKeyPair() {
		return bip32ECKeyPair;
	}
	
	public ECKeyPair getEcKeyPair() {
		return ecKeyPair;
	}

	public BigInteger getPublicKey() {
		return publicKey;
	}
	
	public BigInteger getPrivateKey() {
		return privateKey;
	}
	
	public Credentials getCredentials() {
		return credentials;
	}
	
}