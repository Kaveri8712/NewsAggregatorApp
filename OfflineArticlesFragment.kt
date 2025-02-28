class OfflineArticlesFragment : Fragment() {
    private var _binding: FragmentOfflineArticlesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OfflineArticlesAdapter
    private val articleDao by lazy { NewsDatabase.getDatabase(requireContext()).offlineArticleDao() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfflineArticlesBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeOfflineArticles()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = OfflineArticlesAdapter(
            onItemClicked = { article ->
                val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
                    putExtra("title", article.title)
                    putExtra("description", article.description)
                    putExtra("imageUrl", article.imageUrl)
                    putExtra("publishedAt", article.publishedAt)
                    putExtra("url", article.url)
                    putExtra("isOffline", true)
                }
                startActivity(intent)
            },
            onDeleteClicked = { article ->
                deleteOfflineArticle(article)
            }
        )
        binding.rvOfflineArticles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@OfflineArticlesFragment.adapter
        }
    }

    private fun observeOfflineArticles() {
        lifecycleScope.launch {
            articleDao.getAllArticles().collect { articles ->
                if (articles.isEmpty()) {
                    binding.tvNoArticles.visibility = View.VISIBLE
                    binding.rvOfflineArticles.visibility = View.GONE
                } else {
                    binding.tvNoArticles.visibility = View.GONE
                    binding.rvOfflineArticles.visibility = View.VISIBLE
                    adapter.submitList(articles)
                }
            }
        }
    }

    private fun deleteOfflineArticle(article: OfflineArticle) {
        lifecycleScope.launch {
            articleDao.deleteArticle(article)
            Toast.makeText(context, "Article deleted", Toast.LENGTH_SHORT).show()
        }
    }
} 