package com.studios.amit.dsiofficial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String NOTIF_MESSAGES = "state_movies";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //Creating Views
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private ArrayList<MessageNotification> messageNotifications;
    private JsonArrayRequest jsonArrayRequest;
    private RequestQueue requestQueue;
    String notificationURL = UrlStrings.notificationUrl;
    private OnFragmentInteractionListener mListener;
    SwipeRefreshLayout swipeLayout;
    private LinearLayout notifLinearLayout;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //MenuItem item = menu.findItem(R.id.top_navigation_search);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(User.getIsAdmin()){
            MenuItem menuItem = menu.add("Post Notification");
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    Intent intent = new Intent(getActivity(), PostNotificationActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
        inflater.inflate(R.menu.top_navigation, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.i("ALERT !!", "OPTIONS SELECTED");

        if(id == R.id.top_navigation_profile){
            Intent intent = new Intent(getActivity(), MyProfileActivity.class);
            startActivity(intent);
        }

        if(id == R.id.top_navigation_about){
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        }

        if(id == R.id.top_navigation_logout) {
            //Toast.makeText(this, "DSH BRD", Toast.LENGTH_LONG).show();
            User.setIsLoggedin(false);
            User.removeAllCredentials();

            SharedPreferences sp= getActivity().getSharedPreferences("Login", MODE_PRIVATE);
            SharedPreferences.Editor Ed=sp.edit();
            Ed.putBoolean("isLoggedIn", false);
            Ed.commit();

            Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_notification, container, false);
        setHasOptionsMenu(true);

        //Initializing Views
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                sendAndPrintResponse();
            }
        });
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        messageNotifications = new ArrayList<>();
        sendAndPrintResponse();

        return view;
    }

    private void sendAndPrintResponse()
    {
        String modifiedUrl;

        if(User.getIsAdmin()){
            modifiedUrl = notificationURL + "?year=admin";
        }
        else {
            modifiedUrl = notificationURL + "?year=" + User.getYear();
        }
        //Showing a progress dialog
        final ProgressDialog loading = ProgressDialog.show(this.getContext(),"Loading Data", "Please wait...",false,false);
        requestQueue = VolleySingleton.getInstance(this.getContext()).getRequestQueue(this.getContext());

        jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, modifiedUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i("ALERT !!", response.toString());
                loading.dismiss();
                parseJsonArrayResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.dismiss();
                addRefreshGui();
                Log.i("ALERT ERROR!!", error.toString());
            }
        });

        requestQueue.add(jsonArrayRequest);
    }

    private void addRefreshGui()
    {
        notifLinearLayout = (LinearLayout) getView().findViewById(R.id.new_notif_linear_layout);
        Toast.makeText(getContext(), "Could not connect to Database.\nSwipe down to refresh.", Toast.LENGTH_SHORT).show();
        swipeLayout.setRefreshing(false);
    }

    private void parseJsonArrayResponse(JSONArray jsonArray)
    {
        messageNotifications.clear();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            MessageNotification messageNotification = new MessageNotification();
            JSONObject jsonObject = null;
            try{
                jsonObject = jsonArray.getJSONObject(i);
                messageNotification.setNotificationTitle(jsonObject.getString("messageTitle"));
                messageNotification.setNotificationBody(jsonObject.getString("message"));
                messageNotification.setHasAttachment(jsonObject.getString("hasAttachment"));
                messageNotification.setDateUploaded(jsonObject.getString("DatePosted"));
                if(messageNotification.getHasAttachment().equals("true")){
                    messageNotification.setAttachmentName(jsonObject.getString("attachmentName"));
                    messageNotification.setAttachmentType(jsonObject.getString("attachmentType"));
                }
            }catch (Exception e){e.printStackTrace();}
            messageNotifications.add(messageNotification);
        }



        NotificationManager.numberOfNotifications = messageNotifications.size();

        Intent serviceIntent = new Intent(getActivity(), ServerHeartbeatService.class);
        serviceIntent.putExtra("num", messageNotifications.size());
        serviceIntent.putExtra("year", User.getYear());
        SharedPreferences p = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor Ed= p.edit();
        Ed.putInt("num", messageNotifications.size());
        Ed.putString("year", User.getYear());
        Ed.commit();
        getActivity().startService(serviceIntent);

        //Finally initializing our adapter
        adapter = new CardAdapter(messageNotifications, this.getContext());
        //Adding adapter to recyclerView
        recyclerView.setAdapter(adapter);
        swipeLayout.setRefreshing(false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
           // throw new RuntimeException(context.toString()
             //       + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
