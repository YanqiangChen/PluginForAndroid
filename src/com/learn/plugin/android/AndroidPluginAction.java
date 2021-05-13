package com.learn.plugin.android;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import org.apache.http.util.TextUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AndroidPluginAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
//        Messages.showMessageDialog("Hello World !", "Information", Messages.getInformationIcon());

        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }
        SelectionModel model = mEditor.getSelectionModel();
        final String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText)) {
            return;
        }
        sendRequestWithHttpURLConnection(selectedText, new CallBack() {
            @Override
            public void callback(String str) {
//                Translation translation = gson.fromJson(result, Translation.class);
                YoudaoModel youdaoModel = new Gson().fromJson(str, YoudaoModel.class);
                StringBuilder sb=new StringBuilder();
                sb.append("中文翻译：").append(youdaoModel.getTranslation()).append("   ");
                sb.append("中文翻译：").append(youdaoModel.getTranslation()).append("   ");

                if(youdaoModel.getBasic()!=null && youdaoModel.getBasic().getExplains()!=null){
                    for(String detail:youdaoModel.getBasic().getExplains()){
                        sb.append("不同的含义解释：").append(detail).append("   ");
                    }
                }


                showPopupBalloon(mEditor, sb.toString());

//                Messages.showMessageDialog(str, "Information", Messages.getInformationIcon());
            }
        });


    }

    private void showPopupBalloon(final Editor editor, final String result) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(5000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    public interface CallBack{
        void callback(String str);

    }
    private void sendRequestWithHttpURLConnection(String word,CallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try {
                    URL url=new URL("http://fanyi.youdao.com/openapi.do?keyfrom=Skykai521&key=977124034&type=data&doctype=json&version=1.1&q="+word);
                    connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in=connection.getInputStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null){
                        //在response字符串后面追加字符串
                        response.append(line);
                    }
                    callBack.callback(response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
