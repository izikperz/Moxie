package com.sometimestwo.moxie;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sometimestwo.moxie.Utils.Constants;

import net.dean.jraw.models.Submission;

import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {
    public final static String TAG = Constants.TAG_FRAG_HOME;
    private final static String ARGS_NUM_DISPLAY_COLS = "ARGS_NUM_DISPLAY_COLS";
    private final static int INTENT_LOG_IN = 1;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerMain;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private int mNumDisplayColumns;
    private String mCurrSubreddit;

    private HomeEventListener mHomeEventListener;


    public interface HomeEventListener {
        public void openMediaViewer(Submission submission);

        public void refreshFeed(String fragmentTag);
    }

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  mRedditClient = RedditClient.getInstance();
        //mCurrRedditDataObj = (RedditRequestDataObj) this.getArguments().get(Constants.ARGS_REDDIT_STATE_OBJ);
        unpackArgs();

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        /* Hamburger menu*/
        setupHamburgerMenu(v);

        /* Toolbar setup*/
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_main);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        /* Refresh layout setup*/
        mRefreshLayout = v.findViewById(R.id.recycler_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(FragmentHome.this).get(SubmissionsViewModel.class);
                submissionsViewModel.invalidate();
                mHomeEventListener.refreshFeed(TAG);
            }
        });

        /* Recycler view setup*/
        mRecyclerMain = (RecyclerView) v.findViewById(R.id.recycler_zoomie_view);
        mRecyclerMain.setLayoutManager(new GridLayoutManager(getContext(), mNumDisplayColumns));
        mRecyclerMain.setHasFixedSize(true);

        /* RecyclerView adapter stuff */
        final RecyclerAdapter adapter = new RecyclerAdapter(getContext(), Glide.with(this));

        /* Viewmodel fetching and data updating */
        SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(this).get(SubmissionsViewModel.class);
        submissionsViewModel.postsPagedList.observe(getActivity(), new Observer<PagedList<net.dean.jraw.models.Submission>>() {
            @Override
            public void onChanged(@Nullable PagedList<net.dean.jraw.models.Submission> items) {
                // submitting changes to adapter, if any
                adapter.submitList(items);
            }
        });

        mRecyclerMain.setAdapter(adapter);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mHomeEventListener = (HomeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            //toolbar.setTitle(getResources().getString(R.string.toolbar_title_albums));
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_LOG_IN) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    private void unpackArgs(){
       // SharedPreferencesTokenStore tokenStore = App.getTokenStore();
        try {
            mNumDisplayColumns =  (Integer) this.getArguments().get(ARGS_NUM_DISPLAY_COLS);
          //  mCurrSubreddit = mRedditClient.getmRedditDataRequestObj().getmSubreddit();
        }catch (NullPointerException npe){
            throw new NullPointerException("Null ptr exception trying to unpack arguments in " + TAG);
        }
        // default to 3 if at 0. //TODO: revisit default here
        if( mNumDisplayColumns < 1){
            mNumDisplayColumns = Constants.DEFAULT_NUM_DISPLAY_COLS;
        }
        // default to /r/pics as failsafe if nothing was passed to us
        if(mCurrSubreddit == null ||"".equals(mCurrSubreddit)){
            mCurrSubreddit = Constants.DEFAULT_SUBREDDIT;
        }
    }

    /* Hamburger menu*/
    private void setupHamburgerMenu(View v){
        mDrawerLayout = v.findViewById(R.id.drawer_layout);

        NavigationView navigationView = v.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        handleNavItemSelection(menuItem);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                        Log.e(TAG, "Drawer slide!");
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                        Log.e(TAG, "Drawer opened!");

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                        Log.e(TAG, "Drawer closed!");
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                        Log.e(TAG, "Drawer state changed!");
                    }
                }
        );
    }

    /* Handles left navigation menu item selections*/
    private void handleNavItemSelection(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
           /* case R.id.nav_log_in:
                Intent loginIntent = new Intent(getContext(), ActivityLogin.class);
                //unlockSessionIntent.putExtra("REQUEST_UNLOCK_SESSION", true);
                startActivityForResult(loginIntent,INTENT_LOG_IN);
                return;*/
            case R.id.nav_menu_goto_subreddit:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter subreddit:");

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newSubreddit = input.getText().toString();
                       // mRedditClient.getmRedditDataRequestObj().setmSubreddit(newSubreddit);
                        SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(FragmentHome.this).get(SubmissionsViewModel.class);
                        submissionsViewModel.invalidate();
                        mHomeEventListener.refreshFeed(TAG);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return;

            default:
                Log.e(TAG,"Nav item selection not found! Entered default case!");
        }
    }

    private void saveRecyclerViewState() {

    }

    private void restoreRecyclerViewState() {

    }

    private static DiffUtil.ItemCallback<net.dean.jraw.models.Submission> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<net.dean.jraw.models.Submission>() {
                @Override
                public boolean areItemsTheSame(net.dean.jraw.models.Submission oldItem, net.dean.jraw.models.Submission newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(net.dean.jraw.models.Submission oldItem, net.dean.jraw.models.Submission newItem) {
                    return oldItem.equals(newItem);
                }
            };


    public class RecyclerAdapter extends PagedListAdapter<net.dean.jraw.models.Submission, RecyclerAdapter.ItemViewHolder> {
        private Context mContext;
        private final RequestManager GlideApp;


        RecyclerAdapter(Context mContext, RequestManager glideApp) {
            super(DIFF_CALLBACK);
            this.mContext = mContext;
            this.GlideApp = glideApp;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_thumbnail, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
            net.dean.jraw.models.Submission item = getItem(position);

            if (item != null) {
                GlideApp
                        .load(item.getThumbnail())
                        .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.thumbnailImageView);

                        Log.e("VIDEO_URL", "Submission name: " + item.getTitle()
                                                        + ", Submission url: " + item.getUrl()
                                                        + ", Submission thumbnail: " + item.getThumbnail());

                holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.onClick(holder.thumbnailImageView);
                    }
                });

            } else {
                Toast.makeText(mContext, "Item is null", Toast.LENGTH_LONG).show();
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            //TextView textView;
            ImageView thumbnailImageView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                //textView = itemView.findViewById(R.id.textViewName);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            }

            @Override
            public void onClick(View view) {
                net.dean.jraw.models.Submission submission = getItem(getLayoutPosition());
                mHomeEventListener.openMediaViewer(submission);
            }
        }
    }


}
