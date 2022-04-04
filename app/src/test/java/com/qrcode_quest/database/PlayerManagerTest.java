package com.qrcode_quest.database;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.firebase.firestore.FirebaseFirestore;
import com.qrcode_quest.MockDb;
import com.qrcode_quest.entities.PlayerAccount;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerManagerTest {
    private FirebaseFirestore db;
    private PlayerManager manager;

    public ArrayList<PlayerAccount> createMockAccounts(int size) {
        ArrayList<PlayerAccount> accounts = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            String indexStr = Integer.toString(i);
            String email = "email" + indexStr;
            String phone = "phone" + indexStr;
            boolean isOwner = i == 0;
            boolean isDeleted = i == 1;
            PlayerAccount account = new PlayerAccount(indexStr,
                    email, phone, isDeleted, isOwner);
            accounts.add(account);
        }
        return accounts;
    }

    @Before
    public void initManager() {
        db = MockDb.createMockDatabase(new HashMap<>());
        manager = new PlayerManager(db);
    }

    @Test
    public void testAddPlayer() {
        manager.getPlayer("0", result -> assertNull(result.unwrap()));
        manager.checkUserExists("0", result -> assertFalse(result.unwrap()));
        manager.getPlayerList(result -> assertEquals(0, result.unwrap().size()));

        ArrayList<PlayerAccount> accounts = createMockAccounts(3);
        manager.addPlayer(accounts.get(0), result -> assertTrue(result.isSuccess()));

        manager.getPlayer("0", result -> {
            assertTrue(result.isSuccess());
            assertNotNull(result.unwrap());
            assertEquals("email0", result.unwrap().getEmail());
        });
        manager.checkUserExists("0", result -> assertTrue(result.unwrap()));
        manager.getPlayerList(result -> assertEquals(1, result.unwrap().size()));

         // add another two accounts
        manager.addPlayer(accounts.get(1), result -> assertTrue(result.isSuccess()));
        manager.addPlayer(accounts.get(2), result -> assertTrue(result.isSuccess()));

        manager.getPlayer("0", result -> assertEquals("email0", result.unwrap().getEmail()));
        manager.checkUserExists("0", result -> assertTrue(result.unwrap()));
        manager.getPlayerList(result -> assertEquals(2, result.unwrap().size()));  // deleted player will not be counted
        manager.checkUserExists("1", result -> assertTrue(result.unwrap()));  // but checkUserExists can find them
    }

    @Test
    public void testUpdatePlayer() {
        ArrayList<PlayerAccount> accounts = createMockAccounts(3);
        manager.addPlayer(accounts.get(0), result -> assertTrue(result.isSuccess()));
        manager.addPlayer(accounts.get(1), result -> assertTrue(result.isSuccess()));
        manager.addPlayer(accounts.get(2), result -> assertTrue(result.isSuccess()));
        PlayerAccount thirdPlayer = accounts.get(2);
        thirdPlayer.setEmail("new email");
        manager.updatePlayer(thirdPlayer, result -> assertTrue(result.isSuccess()));
        manager.getPlayer("2", result -> assertEquals("new email", result.unwrap().getEmail()));
    }

    @Test
    public void testDeletePlayer() {
        ArrayList<PlayerAccount> accounts = createMockAccounts(3);
        manager.addPlayer(accounts.get(0), result -> assertTrue(result.isSuccess()));
        manager.addPlayer(accounts.get(1), result -> assertTrue(result.isSuccess()));
        manager.addPlayer(accounts.get(2), result -> assertTrue(result.isSuccess()));
        manager.setDeletedPlayer("2", true, result -> assertTrue(result.isSuccess()));  // can set deleted player deleted
        manager.setDeletedPlayer("1", true, result -> assertTrue(result.isSuccess()));
        manager.getPlayerList(result -> assertEquals(1, result.unwrap().size()));  // only player "1" is left
        manager.setDeletedPlayer("0", true, result -> assertTrue(result.isSuccess()));
        manager.getPlayerList(result -> assertEquals(0, result.unwrap().size()));  // only player "1" is left
    }

    // above are PLAYER relation tests, below are AUTH (authentication) relation tests

    @Test
    public void testAuthentication() {
        manager.validatePlayerSession("device1", "user1", result -> assertFalse(result.unwrap()));
        manager.createPlayerSession("device1", "user1", result -> assertTrue(result.isSuccess()));
        manager.validatePlayerSession("device1", "user1", result -> assertTrue(result.unwrap()));

        // on another unauthenticated device or by a user
        manager.validatePlayerSession("device1", "user2", result -> assertFalse(result.unwrap()));
        manager.validatePlayerSession("device2", "user1", result -> assertFalse(result.unwrap()));

        // create session and test again
        manager.createPlayerSession("device1", "user2", result -> assertTrue(result.isSuccess()));
        manager.createPlayerSession("device2", "user1", result -> assertTrue(result.isSuccess()));
        manager.validatePlayerSession("device1", "user2", result -> assertTrue(result.unwrap()));
        manager.validatePlayerSession("device2", "user1", result -> assertTrue(result.unwrap()));
    }
}
