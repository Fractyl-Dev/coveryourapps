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

public class ChooseContractFragment extends Fragment {
    private RecyclerView templatesRecyclerView;
    private ArrayList<ContractTemplate> templates;
    private CoverCreatorActvity thisActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_contract, container, false);
        thisActivity = (CoverCreatorActvity) getActivity();


        templatesRecyclerView = view.findViewById(R.id.templatesRecyclerView);
        templatesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        templates = thisActivity.getContractTemplates();

//        templates.add(new ContractTemplate("Liability Waiver", "If you die it 100% your fault so don't"));
//        templates.add(new ContractTemplate("Work Contract", "I will work 4 u"));
//        templates.add(new ContractTemplate("Liability Waiver", "If you die it 100% your fault so don't"));

        templatesRecyclerView.setAdapter(new ChooseContractFragment.TemplatesAdapter(templates));


        // Inflate the layout for this fragment
        return view;
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
            Log.d("**Choose Contract Fragment |", "Template ID " + templateDocumentID);
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