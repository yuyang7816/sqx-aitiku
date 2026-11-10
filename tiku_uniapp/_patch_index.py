from pathlib import Path

def patch_index():
    path = Path(r"D:\ideaproject\tiku_all_work\tiku_uniapp\pages\index\index.vue")
    s = path.read_text(encoding="utf-8")
    s = s.replace("import { computed, ref } from 'vue';", "import { computed, ref } from 'vue';\n\timport { storeToRefs } from 'pinia';")
    s = s.replace("\t\tgetNotebookList,\n\t\tgetNotebookMasteryRateText,\n\t\tgetUserProfile,\n\t\tsetPracticeSource", "\t\tgetNotebookMasteryRateText,\n\t\tsetPracticeSource")
    old = "\timport { useTabPageState } from '@/common/use-tab-page-state';\n\n\tuseTabPageState('home');\n\n\tconst quickActions = homeQuickActions;\n\tconst searchPlaceholder = HOME_SEARCH_PLACEHOLDER;\n\tconst profile = ref(getUserProfile());\n\tconst studyStats = ref(getHomeStudyStats());\n\tconst recommendList = ref(getHomeRecommendList());\n\tconst notebook = ref(getNotebookList());"
    new = "\timport { useTabPageState } from '@/common/use-tab-page-state';\n\timport { useUserStore } from '@/stores/user';\n\timport { useNotebookStore } from '@/stores/notebook';\n\n\tuseTabPageState('home');\n\n\tconst userStore = useUserStore();\n\tconst notebookStore = useNotebookStore();\n\tconst { profile } = storeToRefs(userStore);\n\tconst { list: notebookList } = storeToRefs(notebookStore);\n\n\tconst quickActions = homeQuickActions;\n\tconst searchPlaceholder = HOME_SEARCH_PLACEHOLDER;\n\tconst studyStats = ref(getHomeStudyStats());\n\tconst recommendList = ref(getHomeRecommendList());"
    assert old in s, 'index block1'
    s = s.replace(old, new)
    old2 = "\tconst refreshHomeData = () => {\n\t\tprofile.value = getUserProfile();\n\t\tnotebook.value = getNotebookList();\n\t\tstudyStats.value = getHomeStudyStats(todayAnalysis.value);\n\t\trecommendList.value = getHomeRecommendList();\n\t\theroData.value = getHomeHeroData();\n\t};"
    new2 = "\tconst refreshHomeData = () => {\n\t\tstudyStats.value = getHomeStudyStats(todayAnalysis.value);\n\t\trecommendList.value = getHomeRecommendList();\n\t\theroData.value = getHomeHeroData();\n\t};"
    assert old2 in s, 'index block2'
    s = s.replace(old2, new2)
    s = s.replace('getNotebookMasteryRateText(notebook.value)', 'getNotebookMasteryRateText(notebookList.value)')
    path.write_text(s, encoding='utf-8')
    print('index ok')

patch_index()
