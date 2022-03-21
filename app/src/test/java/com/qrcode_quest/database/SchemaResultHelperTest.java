package com.qrcode_quest.database;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.qrcode_quest.entities.PlayerAccount;
import com.qrcode_quest.entities.QRCode;
import com.qrcode_quest.entities.QRShot;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class SchemaResultHelperTest {
    ArrayList<PlayerAccount> dummyAccounts;
    ArrayList<QRShot> dummyShots;

    @Before
    public void setup() {
        dummyAccounts = new ArrayList<>();
        dummyShots = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            dummyAccounts.add(new PlayerAccount("n" + i, "email" + i, "phone" + i));
        }
        // 3 qr codes that are shot by both player 1 and player 2
        for (int i = 1; i <= 3; i++) {
            String qrHash = "code1";
            dummyShots.add(new QRShot("n1", qrHash));
            dummyShots.add(new QRShot("n2", qrHash));
        }
        // a qr code only shot by player 1
        dummyShots.add(new QRShot("n1", "code2"));
    }

    @Test
    public void testEmptyInput() {
        HashMap<String, ArrayList<QRShot>> map1 =
                SchemaResultHelper.getOwnerNameToShotArrayMapFromJoin(new ArrayList<>(), new ArrayList<>());
        assertEquals(0, map1.size());
        HashMap<String, QRCode> map2 = SchemaResultHelper.getQrHashToCodeMapFromCodes(new ArrayList<>());
        assertEquals(0, map2.size());
        HashMap<String, QRCode> map3 = SchemaResultHelper.getQrHashToCodeMapFromShots(new ArrayList<>());
        assertEquals(0, map3.size());
        HashMap<String, ArrayList<QRShot>> map4 = SchemaResultHelper.getQrHashToShotArrayMap(new ArrayList<>());
        assertEquals(0, map4.size());
    }

    @Test
    public void testOwnerShotJoin() {
        HashMap<String, ArrayList<QRShot>> map =
                SchemaResultHelper.getOwnerNameToShotArrayMapFromJoin(dummyAccounts, dummyShots);

        // size test, should contain the owner that has no qr shots in the account
        assertEquals(3, map.size());

        // each player should have correct # of qr shots
        assertEquals(4, map.get("n1").size());
        assertEquals(3, map.get("n2").size());
        assertEquals(0, map.get("n3").size());

        // the map is expected to contain references instead of copies
        assertTrue(map.get("n1").contains(dummyShots.get(0)));
        assertTrue(map.get("n2").contains(dummyShots.get(1)));
    }
}
