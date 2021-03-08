package zk.obd;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    RelativeLayout rl_score_info;
    FrameLayout frameLayout;
    ImageView rightCheckLight;
    ImageView leftCheckLight;
    TranslateAnimation right_translateAnimation;
    TranslateAnimation translateAnimationLeft;
    ImageView carCheckLightBody;
    AnimationDrawable animationDrawableBody;
    ImageView dialog_wharp;
    ImageView dialog_semi_circle;
    ImageView dialog_semi_circle_smill;
    Animation animation_rotate_dialog;
    Animation animation_rotate2_dialog;
    Animation animation_rotate3_dialog;
    FrameLayout dialog_fragment;

    TextView mileage_number;
    TextView oil_number;
    TextView car_number;
    TextView voltage_number;
    TextView vin_number;
    TextView ble_State;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_check);
        initView();
        initDate();
        initAnimation();
        initClick();
//        showDialog();

    }

    private void initView() {
        rl_score_info = findViewById(R.id.rl_score_info);
        frameLayout = findViewById(R.id.dialog);
        carCheckLightBody = findViewById(R.id.iv_control_normal_bg);
        leftCheckLight = (ImageView) findViewById(R.id.iv_scan_left);
        rightCheckLight = (ImageView) findViewById(R.id.iv_scan_right);
        //dialog
        dialog_fragment = findViewById(R.id.dialog);
        dialog_wharp = (ImageView) findViewById(R.id.loding_wharp);
        dialog_semi_circle = (ImageView) findViewById(R.id.loding_b);
        dialog_semi_circle_smill = (ImageView) findViewById(R.id.loding_1);
        //数据
        mileage_number= findViewById(R.id.mileage_txt_number);
        oil_number= findViewById(R.id.car_oil_txt_number);
        car_number=findViewById(R.id.car_txt_modle);
         voltage_number=findViewById(R.id.voltage_txt_number);
         vin_number=findViewById(R.id.vin_txt_number);
         ble_State=findViewById(R.id.lanya);
    }

    public void initAnimation() {

        //左车刷
        translateAnimationLeft = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim_guang_shua_left);
        //右车刷
        right_translateAnimation = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim_guang_shua_right);
        //dialog
        animation_rotate_dialog = AnimationUtils.loadAnimation(this, R.anim.zheng_rotate_anim);
        animation_rotate2_dialog = AnimationUtils.loadAnimation(this, R.anim.fan_rotate_anim);
        animation_rotate3_dialog = AnimationUtils.loadAnimation(this, R.anim.da_fan_rotate_anim);
    }

    public void startAnimation() {
        leftCheckLight.setVisibility(View.VISIBLE);
        //车身扫描
        carCheckLightBody.setImageResource(R.drawable.car_check_light_body);
        animationDrawableBody = (AnimationDrawable) carCheckLightBody.getDrawable();
        animationDrawableBody.start();
        //车摆动
        leftCheckLight.startAnimation(translateAnimationLeft);


    }

    public void initClick() {
        right_translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {


                rightCheckLight.clearAnimation();
                rightCheckLight.setVisibility(View.GONE);

                leftCheckLight.setVisibility(View.VISIBLE);
                leftCheckLight.setAnimation(translateAnimationLeft);
            }
        });

        translateAnimationLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {


            }

            @Override
            public void onAnimationEnd(Animation animation) {


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

                leftCheckLight.clearAnimation();
                leftCheckLight.setVisibility(View.GONE);

                rightCheckLight.setVisibility(View.VISIBLE);
                rightCheckLight.setAnimation(right_translateAnimation);
            }
        });

        //检测
        rl_score_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //首先弹出转圈圈
                dialog_fragment.setVisibility(View.GONE);
                //检车
                startAnimation();
            }
        });
    }

    public void showDialog() {
        dialog_fragment.setVisibility(View.VISIBLE);
        //开启动画
        dialog_wharp.setAnimation(animation_rotate3_dialog);
        dialog_semi_circle.setAnimation(animation_rotate2_dialog);
        dialog_semi_circle_smill.setAnimation(animation_rotate_dialog);


    }

    public void initDate()
    {
         mileage_number.setText("");
         oil_number.setText("");
         car_number.setText("");
         voltage_number.setText("");
         vin_number.setText("");
        //初始话数据扫描动态图
        settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,"");


    }

    public  void settingDrawableTop(Context context, TextView view , int resourcesDrawable, String  resourcesString) {
        view.setVisibility(View.VISIBLE);
        Drawable drawable = null;
        drawable = context.getResources().getDrawable(resourcesDrawable);
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
            view.setCompoundDrawables(null, null, drawable, null);//图在上边
            view.setText(resourcesString);
        }
    }
}
