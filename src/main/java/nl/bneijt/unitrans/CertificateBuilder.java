package nl.bneijt.unitrans;

import org.joda.time.DateTime;
import sun.security.x509.*;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateBuilder {


    public static final String ALGORITHM = "SHA256withRSA";

    /**
     * Create a self-signed X.509 Certificate
     * @param fqdn fully qualified domain name
     * @param keypair the KeyPair
     * @param days how many days from now the Certificate is valid for
     */
    public static X509Certificate generateCertificate(String fqdn, KeyPair keypair, int days)
            throws GeneralSecurityException, IOException
    {



        PrivateKey key = keypair.getPrivate();

        // Prepare the information required for generating an X.509 certificate.
        X509CertInfo info = new X509CertInfo();
        X500Name owner = new X500Name("CN=" + fqdn);
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new BigInteger(64, new SecureRandom())));
        try {
            info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        } catch (CertificateException ignore) {
            info.set(X509CertInfo.SUBJECT, owner);
        }
        try {
            info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        } catch (CertificateException ignore) {
            info.set(X509CertInfo.ISSUER, owner);
        }
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + days * 86400000l);

        info.set(X509CertInfo.VALIDITY, new CertificateValidity(notBefore, notAfter));
        info.set(X509CertInfo.KEY, new CertificateX509Key(keypair.getPublic()));
        info.set(X509CertInfo.ALGORITHM_ID,
                new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(key, ALGORITHM);

        // Update the algorithm and sign again.
        info.set(CertificateAlgorithmId.NAME + '.' + CertificateAlgorithmId.ALGORITHM, cert.get(X509CertImpl.SIG_ALG));
        cert = new X509CertImpl(info);
        cert.sign(key, ALGORITHM);
        cert.verify(keypair.getPublic());

        return cert;


    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(2048, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    public static void generateKeyStore(File keystoreLocation, String password) throws IOException {
        try {


            KeyPair keyPair = generateRSAKeyPair();
            X509Certificate cert = generateCertificate("unitrans.local", keyPair, 10);


            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            keyStore.setKeyEntry("selfsigned",
                    keyPair.getPrivate(),
                    password.toCharArray(),
                    new java.security.cert.X509Certificate[]{cert});

            // Store away the keystore.
            try(FileOutputStream fos = new FileOutputStream(keystoreLocation)) {
                keyStore.store(fos, password.toCharArray());
            };



        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IOException("Algorithm or provider failure", e);
        } catch (SignatureException | CertificateException e) {
            throw new IOException("Certificate failure", e);
        } catch (KeyStoreException | InvalidKeyException e) {
            throw new IOException("Key failure", e);
        } catch (GeneralSecurityException e) {
            throw new IOException("Key failure", e);
        }

    }
}
