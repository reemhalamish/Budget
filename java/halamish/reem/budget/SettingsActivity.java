package halamish.reem.budget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Set;

import halamish.reem.budget.main.MainActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHUTDOWN_THE_APP_EXTRA = "shut_me_down";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Settings settings = Settings.getInstance();
        final Context context = this;


        initiateBalanceCbx(context, settings);
        initiateLanguageInput(context, settings);



    }

    private void initiateLanguageInput(final Context context, final Settings settings) {
        RadioGroup rg = (RadioGroup) findViewById(R.id.rg_settings_radio_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                boolean languageChanged = false;
                if (!((RadioButton) findViewById(id)).isChecked())
                    return;
                switch (id) {
                    case R.id.rb_settings_lng_heb_uni:
                        languageChanged = settings.setUserLanguage(Settings.Language.HEBREW_UNISEX);
                        break;
                    case R.id.rb_settings_lng_heb_fem:
                        languageChanged = settings.setUserLanguage(Settings.Language.HEBREW_FEMALE);
                        break;
                    case R.id.rb_settings_lng_heb_male:
                        Toast.makeText(context, "TODO!", Toast.LENGTH_SHORT).show(); // TODO implement
                        return;
                    case R.id.rb_settings_lng_eng:
                        languageChanged = settings.setUserLanguage(Settings.Language.ENGLISH);
                        break;
                }
                if (languageChanged) {
                    forceReload();
                }
            }
        });

        int radioButtonID = getRadioButtonIdBasedOnLanguage(settings);
        rg.check(radioButtonID);


        findViewById(R.id.btn_settings_close_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent shutDownAppIntent = new Intent(SettingsActivity.this, MainActivity.class);
                shutDownAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                shutDownAppIntent.putExtra(SHUTDOWN_THE_APP_EXTRA, true);
                startActivity(shutDownAppIntent);
            }
        });
    }

    private int getRadioButtonIdBasedOnLanguage(Settings settings) {
        Settings.Language userLanguage = settings.getUserLanguage();
        if (userLanguage == Settings.Language.ENGLISH)
                return R.id.rb_settings_lng_eng;
        else if (userLanguage == Settings.Language.HEBREW_UNISEX)
            return R.id.rb_settings_lng_heb_uni;
        else if (userLanguage == Settings.Language.HEBREW_FEMALE)
            return R.id.rb_settings_lng_heb_fem;
        else // male
            return R.id.rb_settings_lng_heb_male;
    }

    private void initiateBalanceCbx(final Context context, final Settings settings) {
        ((CheckBox) findViewById(R.id.cbx_settings_keep_from_last)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean newState) {
                settings.setKeepBalanceFromLast(newState);
                final String msg;
                if (newState)
                    msg = getString(R.string.settings_toast_balance_saved);
                else
                    msg = getString(R.string.settings_toast_balance_reset);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.iv_settings_qmark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setMessage(R.string.settings_explanation_keep_balance)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });


    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
//        Settings.getInstance().updateLocal();
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void forceReload() {
        finish();
        startActivity(getIntent());
    }
}
