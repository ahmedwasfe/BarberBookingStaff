package com.ahmet.barberbookingstaff.ui.service;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahmet.barberbookingstaff.R;
import com.ahmet.barberbookingstaff.adapter.ServicesAdaper;
import com.ahmet.barberbookingstaff.common.Common;
import com.ahmet.barberbookingstaff.model.Services;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ahmed.com.swiperecyclerview.MButton;
import ahmed.com.swiperecyclerview.SwipeRecyclerViewHelper;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class ServicesFragment extends Fragment {

    @BindView(R.id.recycler_services)
    RecyclerView mRecyclerServices;
    @BindView(R.id.shimmer_layout_services)
    ShimmerLayout mShimmerLayout;


    private ServicesViewModel servicesViewModel;
    private ServicesAdaper servicesAdaper;

    private static ServicesFragment instance;
    public static ServicesFragment getInstance(){
        return instance == null ? new ServicesFragment() : instance;
    }

    @OnClick(R.id.fab_add_services) void onFabClick(){
        Common.setFragment(AddServiceFragmnet.getInstance(), R.id.frame_layout_home,
                getActivity().getSupportFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        servicesViewModel = new ViewModelProvider(this).get(ServicesViewModel.class);
        View layoutView = inflater.inflate(R.layout.fragment_services, container, false);

        ButterKnife.bind(this, layoutView);

        initViews();

        servicesViewModel.getMutableMessageError()
                .observe(getViewLifecycleOwner(), error -> {
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                });

        servicesViewModel.getListMutableService()
                .observe(getViewLifecycleOwner(), services -> {

                    mShimmerLayout.stopShimmerAnimation();
                    mShimmerLayout.setVisibility(View.GONE);

                    servicesAdaper = new ServicesAdaper(getActivity(), services);
                    mRecyclerServices.setAdapter(servicesAdaper);
                });

        return layoutView;
    }

    private void initViews() {

        mShimmerLayout.startShimmerAnimation();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerServices.setHasFixedSize(true);
        mRecyclerServices.setLayoutManager(layoutManager);
        mRecyclerServices.addItemDecoration(new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));

        SwipeRecyclerViewHelper recyclerViewHelper = new SwipeRecyclerViewHelper(
                getActivity(), mRecyclerServices, 150
        ) {
            @Override
            public void instantiateButton(RecyclerView.ViewHolder viewHolder, List<MButton> mListMButton) {

                mListMButton.add(new MButton(getActivity(), getString(R.string.delete),24,0,
                        getActivity().getColor(R.color.colorRed), position -> {

                    Services services = servicesAdaper.getItemAtPosition(position);
                    showDilogDeleteService(services);

                }));

                mListMButton.add(new MButton(getActivity(), getString(R.string.update),24,0,
                        getActivity().getColor(R.color.colorGreen), position -> {

                    Services services = servicesAdaper.getItemAtPosition(position);
                    showDilogUpdateService(services);

                }));
            }
        };
    }

    private void showDilogUpdateService(Services services) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View layoutVIew = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_update_service, null);
        builder.setView(layoutVIew);

        EditText mInputName = layoutVIew.findViewById(R.id.input_update_service_name);
        EditText mInputPrice = layoutVIew.findViewById(R.id.input_update_service_price);

        mInputName.setText(services.getServiceName());
        mInputPrice.setText(String.valueOf(services.getServicePrice()));

        AlertDialog dialog = builder.create();
        Common.customDialog(dialog);

        layoutVIew.findViewById(R.id.btn_update_service)
                .setOnClickListener(v -> {

                    String name = mInputName.getText().toString();
                    String price = mInputPrice.getText().toString();

                    if (TextUtils.isEmpty(name)){
                        mInputName.setError(getString(R.string.please_enter_service_name));
                        return;
                    }

                    Services service = services;
                    service.setServiceName(name);
                    service.setServicePrice(TextUtils.isEmpty(price) ? 0 : Long.parseLong(price));

                    Map<String, Object> mapUpdateService = new HashMap<>();
                    mapUpdateService.put("serviceName", service.getServiceName());
                    mapUpdateService.put("servicePrice", service.getServicePrice());

                    FirebaseDatabase.getInstance().getReference()
                            .child(Common.KEY_AllSALON_REFERANCE)
                            .child(Common.currentSalon.getSalonId())
                            .child(Common.KEY_SERICES_REFERANCE)
                            .child(services.getId())
                            .updateChildren(mapUpdateService)
                            .addOnCompleteListener(task -> {
                               if (task.isSuccessful()){
                                   dialog.dismiss();
                                   servicesViewModel.loadAllServices();
                                   Toast.makeText(getActivity(), getString(R.string.update_service_success), Toast.LENGTH_SHORT).show();
                               }
                            });

                });

        layoutVIew.findViewById(R.id.btn_cancel_update_service)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                });

        dialog.show();

    }

    private void showDilogDeleteService(Services services) {

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_service)
                .setMessage(R.string.confirm_delete_service)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).setPositiveButton(R.string.delete, (dialog, which) -> {
            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_AllSALON_REFERANCE)
                    .child(Common.currentSalon.getSalonId())
                    .child(Common.KEY_SERICES_REFERANCE)
                    .child(services.getId())
                    .removeValue()
                    .addOnCompleteListener(task -> {
                        servicesViewModel.loadAllServices();
                        Toast.makeText(getActivity(), getString(R.string.delete_service_success), Toast.LENGTH_SHORT).show();
                    });
        }).show();
    }
}
