package com.klipspringercui.sgbusgo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kevin on 13/3/17.
 */

class ETADRecyclerViewAdapter extends RecyclerView.Adapter<ETADRecyclerViewAdapter.ETAViewHolder> {

    private static final String TAG = "ETADRecyclerViewAdapter";

    private List<ETAItem> etas;
    private BusStop busStop;

    static class ETAViewHolder extends RecyclerView.ViewHolder {

        TextView txtBusStop = null;
        TextView txtBusServiceNo = null;
        TextView txtNextETA = null;
        TextView txtSubETA = null;
        TextView txtSub3ETA = null;

        public ETAViewHolder(View view) {
            super(view);
            txtBusServiceNo = (TextView) view.findViewById(R.id.txtETADServiceNo);
            txtBusStop = (TextView) view.findViewById(R.id.txtETADBusStop);
            txtNextETA = (TextView) view.findViewById(R.id.txtETADNext);
            txtSubETA = (TextView) view.findViewById(R.id.txtETADSub);
            txtSub3ETA = (TextView) view.findViewById(R.id.txtETADSub3);
        }

    }

    public ETADRecyclerViewAdapter(List<ETAItem> etas, BusStop busStop) {
        this.etas = etas;
        this.busStop = busStop;
    }

    @Override
    public ETAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_etad_browse, parent, false);
        return new ETAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ETAViewHolder holder, int position) {
        if (this.etas != null) {
            ETAItem item = etas.get(position);
            holder.txtBusServiceNo.setText(item.getServiceNo());
            holder.txtBusStop.setText(busStop.getDescription());
            if (item.getArrival1() == null || item.getArrival1().length() == 0 || item.getArrival1().equals("0")) {
                holder.txtNextETA.setText("No operating buses");
                holder.txtSubETA.setText("");
                holder.txtSub3ETA.setText("");
                return;
            }
            holder.txtNextETA.setText(item.getArrival1());
            holder.txtSubETA.setText(item.getArrival2());
            holder.txtSub3ETA.setText(item.getArrival3());
        }
    }

    @Override
    public int getItemCount() {
        return (this.etas == null)? 0 : this.etas.size();
    }

    public void loadNewData(List<ETAItem> data, BusStop busStop) {
        this.etas = data;
        this.busStop = busStop;
        notifyDataSetChanged();
    }


}