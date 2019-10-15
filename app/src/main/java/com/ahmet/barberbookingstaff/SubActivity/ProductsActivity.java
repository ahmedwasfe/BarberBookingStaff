package com.ahmet.barberbookingstaff.SubActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;

import com.ahmet.barberbookingstaff.Common.Common;
import com.ahmet.barberbookingstaff.Fragments.AddProductFragment;
import com.ahmet.barberbookingstaff.Fragments.AddServiceFragmnet;
import com.ahmet.barberbookingstaff.Fragments.ShowProductFragment;
import com.ahmet.barberbookingstaff.R;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ProductsActivity extends AppCompatActivity {


    private Unbinder mUnbinder;

    @BindView(R.id.btn_app_bar_product)
    BottomAppBar mBottomAppBarProducts;

    @OnClick(R.id.fab_add_product)
    void showProduct(){
        Common.setFragment(new AddProductFragment(), R.id.frame_layout_product, getSupportFragmentManager());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        mUnbinder = ButterKnife.bind(this);

        mBottomAppBarProducts.replaceMenu(R.menu.app_bar_products_menu);
        mBottomAppBarProducts.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                switch (id){
                    case R.id.nav_show_products:
                        Common.setFragment(new ShowProductFragment(), R.id.frame_layout_product, getSupportFragmentManager());
                        break;
                    case R.id.nav_add_service:
                        Common.setFragment(new AddServiceFragmnet(), R.id.frame_layout_product, getSupportFragmentManager());
                }
                return false;
            }
        });

        Common.setFragment(new ShowProductFragment(), R.id.frame_layout_product, getSupportFragmentManager());



    }
}
