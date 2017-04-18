package share.caixinlib.com.caixinsharelib;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.caixinnews.share.CXShareEntity;
import com.caixinnews.share.CaixinShare;
import com.caixinnews.share.ICXShareCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView sharetoFB ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharetoFB = (TextView) findViewById(R.id.sharetoFB);
        sharetoFB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sharetoFB:
                CXShareEntity entity = new CXShareEntity();
                entity.imagePath = "https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/bd_logo1_31bdc765.png";
                entity.title  = "title title title title title title title title title title ";
                entity.summary = "summary summary summary summary summary summary summary summary ";
                entity.url = "http://www.baidu.com";
                CaixinShare share = new CaixinShare(this);
                break;
        }

    }
}
