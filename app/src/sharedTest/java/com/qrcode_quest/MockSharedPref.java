package com.qrcode_quest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

public class MockSharedPref {
    static public SharedPreferences createMockSharedPref(HashMap<String, String> content) {
        SharedPreferences prefs = mock(SharedPreferences.class);

        when(prefs.contains(anyString())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                String key = invocation.getArgument(0);
                return content.containsKey(key);
            }
        });

        when(prefs.getString(anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String key = invocation.getArgument(0);
                if (content.containsKey(key))
                    return content.get(key);
                else
                    return invocation.getArgument(1);  // default
            }
        });

        when(prefs.edit()).thenAnswer(new Answer<SharedPreferences.Editor>() {
            @Override
            public SharedPreferences.Editor answer(InvocationOnMock invocation) throws Throwable {
                HashMap<String, String> editorContent = new HashMap<>(content);
                return createMockSharedPrefEditor(content, editorContent);
            }
        });

        return prefs;
    }

    static public SharedPreferences.Editor createMockSharedPrefEditor(HashMap<String, String> prefContent,
                                                            HashMap<String, String> editorContent) {
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);

        when(editor.putString(anyString(), anyString())).thenAnswer((Answer<Void>) invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            editorContent.put(key, value);
            return null;
        });

        // copy the content from editor to pref when apply() is called
        doAnswer(invocation -> {
            prefContent.clear();
            for (String key: editorContent.keySet())
                prefContent.put(key, editorContent.get(key));
            return null;
        }).when(editor).apply();

        return editor;
    }
}
