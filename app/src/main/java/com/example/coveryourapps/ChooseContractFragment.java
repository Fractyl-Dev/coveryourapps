package com.example.coveryourapps;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChooseContractFragment extends Fragment implements View.OnClickListener{
    private RecyclerView templatesRecyclerView;
    private Button writeYourOwnButton;
    private CoverCreatorActvity thisActivity;
    private ArrayList<ContractTemplate> contractTemplates;


    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_contract, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();
        templatesRecyclerView = view.findViewById(R.id.templatesRecyclerView);
        templatesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        writeYourOwnButton = view.findViewById(R.id.writeYourOwnButton);
        writeYourOwnButton.setOnClickListener(this);

        contractTemplates = new ArrayList<>();
        updateContractTemplatesUI();
        templatesRecyclerView.setAdapter(new ChooseContractFragment.TemplatesAdapter(DBHandler.getAllContractTemplates()));


        // Inflate the layout for this fragment
        return view;
    }

    public void updateContractTemplatesUI() {
        if (contractTemplates != null) {
            ArrayList<ContractTemplate> newContractTemplates = new ArrayList<>(DBHandler.getAllContractTemplates());

            //Only update if there is a difference, this allows for updating in the background with nothing happening if nothing is new
            if (!contractTemplates.toString().equals(newContractTemplates.toString())) {
                contractTemplates.clear();
                contractTemplates.addAll(newContractTemplates);
                templatesRecyclerView.setAdapter(new ChooseContractFragment.TemplatesAdapter(contractTemplates));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.writeYourOwnButton){
            Log.d("**Choose Concract Fragment |", "Wh " + thisActivity.getSelectedRecipients());

            thisActivity.changeCoverCreatorLayover(thisActivity.getWriteAContractFragment(), "writeAContractFragment");
            thisActivity.setToolbarTopText("Write a Contract");
        }
    }

    class TemplatesAdapter extends RecyclerView.Adapter<ChooseContractFragment.TemplateViewHolder> {
        private ArrayList<ContractTemplate> templates;

        public TemplatesAdapter(ArrayList<ContractTemplate> templates) {
            super();
            this.templates = templates;
        }

        @NonNull
        @Override
        public ChooseContractFragment.TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChooseContractFragment.TemplateViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ChooseContractFragment.TemplateViewHolder holder, int position) {
            holder.bind(this.templates.get(position));
        }

        @Override
        public int getItemCount() {
            return this.templates.size();
        }
    }

    class TemplateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ContractTemplate template;
        private String templateDocumentID;
        private TextView contractTitle;
        private Button contractSelectButton;

        public TemplateViewHolder(ViewGroup container) {
            super(LayoutInflater.from(getContext()).inflate(R.layout.choose_contract_list_item, container, false));
            contractTitle = itemView.findViewById(R.id.contractTitle);
            contractSelectButton = itemView.findViewById(R.id.contractSelectButton);
        }

        public void bind(ContractTemplate template) {
            this.template = template;

            contractTitle.setText(template.getTitle());
            contractSelectButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.contractSelectButton) {
                thisActivity.setCurrentContractTemplate(template);
                thisActivity.changeCoverCreatorLayover(thisActivity.getContractTemplateOverviewFragment(), "contractTemplateOverviewFragment");
            }
        }
    }
}