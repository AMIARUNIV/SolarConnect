package first.Project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HomePagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        viewPager.setOffscreenPageLimit(2);

        pagerAdapter = new HomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Disable swipe when map fragment is active
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (pagerAdapter.isMapPage(position)) {
                    // Disable swipe
                    viewPager.setUserInputEnabled(false);
                } else {
                    // Enable swipe
                    viewPager.setUserInputEnabled(true);
                }
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Map");
            else tab.setText("Products");
        }).attach();

        return view;
    }
}
