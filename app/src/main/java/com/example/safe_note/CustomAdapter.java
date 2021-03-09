package com.example.safe_note;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder>{

    private final LayoutInflater inflater;
    public static ArrayList<EditRecyclerView> editRecyclerViewArrayList;

    public CustomAdapter(Context context, ArrayList<EditRecyclerView> editRecyclerViewArrayList){

        inflater = LayoutInflater.from(context);
        CustomAdapter.editRecyclerViewArrayList = editRecyclerViewArrayList;
    }

    @NonNull
    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {

        holder.editText.setText(editRecyclerViewArrayList.get(position).getEditTextValue());
        Log.d("print", "yes");
    }

    @Override
    public int getItemCount() {
        return editRecyclerViewArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        protected EditText editText;

        public MyViewHolder(View itemView) {
            super(itemView);

            editText = (EditText) itemView.findViewById(R.id.editId);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    editRecyclerViewArrayList.get(getAdapterPosition()).setEditTextValue(editText.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}
