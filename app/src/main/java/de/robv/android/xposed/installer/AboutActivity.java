package de.robv.android.xposed.installer;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.io.InputStream;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import de.robv.android.xposed.installer.util.NavUtil;
import de.robv.android.xposed.installer.util.ThemeUtil;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_TEXT;

public class AboutActivity extends XposedBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_item_about);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, R.string.details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new AboutFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent sharingIntent = new Intent(ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(EXTRA_TEXT, getString(R.string.share_app_text, getString(R.string.support_material_xda)));
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));

        return super.onOptionsItemSelected(item);
    }

    public static class AboutFragment extends Fragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.tab_about, container, false);

            View changelogView = v.findViewById(R.id.changelogView);
            View developersView = v.findViewById(R.id.developersView);
            View licensesView = v.findViewById(R.id.licensesView);
            View translatorsView = v.findViewById(R.id.translatorsView);
            View sourceCodeView = v.findViewById(R.id.sourceCodeView);

            String packageName = getActivity().getPackageName();
            String translator = getResources().getString(R.string.translator);

            String changes = null;
            try {
                InputStream is = getContext().getAssets().open("changelog.html");
                int size = is.available();

                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                changes = new String(buffer);
            } catch (IOException ignored) {
            }

            if (changes == null) {
                changelogView.setVisibility(View.GONE);
            } else {
                final String finalChanges = changes;
                changelogView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(getContext())
                                .title(R.string.changes)
                                .content(Html.fromHtml(finalChanges))
                                .positiveText(android.R.string.ok).show();
                    }
                });
            }

            try {
                String version = getActivity().getPackageManager().getPackageInfo(packageName, 0).versionName;
                ((TextView) v.findViewById(R.id.app_version)).setText(version);
            } catch (NameNotFoundException ignored) {
            }

            licensesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createLicenseDialog();
                }
            });

            developersView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                            .title(R.string.about_developers_label)
                            .content(R.string.about_developers)
                            .positiveText(android.R.string.ok)
                            .show();

                    ((TextView) dialog.findViewById(R.id.content)).setMovementMethod(LinkMovementMethod.getInstance());
                }
            });

            sourceCodeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtil.startURL(getActivity(), getString(R.string.about_source));
                }
            });

            if (translator.isEmpty()) {
                translatorsView.setVisibility(View.GONE);
            }

            return v;
        }

        private void createLicenseDialog() {
            Notices notices = new Notices();
            notices.addNotice(new Notice("material-dialogs", "https://github.com/afollestad/material-dialogs", "Copyright (c) 2014-2016 Aidan Michael Follestad", new MITLicense()));
            notices.addNotice(new Notice("StickyListHeaders", "https://github.com/emilsjolander/StickyListHeaders", "Emil Sjölander", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("PreferenceFragment-Compat", "https://github.com/Machinarius/PreferenceFragment-Compat", "machinarius", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("libsuperuser", "https://github.com/Chainfire/libsuperuser", "Copyright (C) 2012-2015 Jorrit \"Chainfire\" Jongma", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("picasso", "https://github.com/square/picasso", "Copyright 2013 Square, Inc.", new ApacheSoftwareLicense20()));

            new LicensesDialog.Builder(getContext())
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show();
        }
    }
}