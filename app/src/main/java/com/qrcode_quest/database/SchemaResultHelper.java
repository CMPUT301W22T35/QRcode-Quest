package com.qrcode_quest.database;

import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;
import com.qrcode_quest.entities.RawQRCode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * responsible for performing the conversion between Lists, HashMaps and among different data types
 * that are covered by the schema
 */
public class SchemaResultHelper {
    /**
     * converts a list of QRCode to a hash map from qrHash to QRCode; input cannot contain codes
     * with repeat hash
     * @param codes a list of QRCode objects
     * @return a hash map mapping from qrHash to all original codes
     */
    public static HashMap<String, QRCode> getQrHashToCodeMapFromCodes(ArrayList<QRCode> codes) {
        HashMap<String, QRCode> map = new HashMap<>();
        for (QRCode code: codes) {
            assert(!map.containsKey(code.getHashCode()));
            map.put(code.getHashCode(), code);
        }
        return map;
    }

    public static HashMap<String, QRCode> getQrHashToCodeMapFromShots(ArrayList<QRShot> shots) {
        HashMap<String, QRCode> map = new HashMap<>();
        for(QRShot shot: shots) {
            String qrHash = shot.getCodeHash();
            if(!map.containsKey(qrHash)) {
                map.put(qrHash, new QRCode(qrHash, RawQRCode.getScoreFromHash(qrHash)));
            }
        }
        return map;
    }

    public static HashMap<String, ArrayList<QRShot>> getQrHashToShotArrayMap(ArrayList<QRShot> shots) {
        HashMap<String, ArrayList<QRShot>> map = new HashMap<>();
        for(QRShot shot: shots) {
            String qrHash = shot.getCodeHash();
            ArrayList<QRShot> codeShots;
            if(!map.containsKey(qrHash)) {
                codeShots = new ArrayList<>();
                map.put(qrHash, codeShots);
            } else {
                codeShots = map.get(qrHash);
            }
            codeShots.add(shot);
        }
        return map;
    }

    /**
     * left joins accounts and shots so that each player account has a list of corresponding QRShot
     * available for lookup
     * @param accounts a list of accounts to join
     * @param shots a list of shots to join
     * @return the result of a join, mapping from a owner's name to a list of QRShot objects
     */
    public static HashMap<String, ArrayList<QRShot>> getOwnerNameToShotArrayMapFromJoin(
            ArrayList<PlayerAccount> accounts, ArrayList<QRShot> shots) {
        HashMap<String, ArrayList<QRShot>> map = new HashMap<>();
        for (PlayerAccount account: accounts) {
            map.put(account.getUsername(), new ArrayList<>());
        }
        for(QRShot shot: shots) {
            if(map.containsKey(shot.getOwnerName())) {
                Objects.requireNonNull(map.get(shot.getOwnerName())).add(shot);
            }
        }
        return map;
    }
}
