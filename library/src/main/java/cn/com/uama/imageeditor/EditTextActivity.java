package cn.com.uama.imageeditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by liwei on 2017/3/18 17:21
 * Email: liwei@uama.com.cn
 * Description: 编辑文字界面
 */

public class EditTextActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lm_image_editor_activity_edit_text);

        editText = (EditText) findViewById(R.id.edit_text);
        // 获取传递过来的文字
        String text = getIntent().getStringExtra("text");
        if (text != null) {
            editText.setText(text);
            editText.setSelection(text.length());
        }

        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_done).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_cancel) {
            finish();
        } else if (id == R.id.button_done) {
            String text = editText.getText().toString().trim();
            Intent data = new Intent();
            data.putExtra("text", text);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.lm_image_editor_edit_text_from_top_down);
    }
}
