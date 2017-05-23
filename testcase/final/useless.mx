int func(int a, int b, int c)
{
	return a + b + c;
}

int main()
{
	int n = getInt();
	int[][] f = new int[n][n];
	int[][] g = new int[n][n];
	int[][] g_useless = new int[n][n];
	int i; int j; int k;
	for( i = 0; i < n; ++i)
		for( j = 0; j < n; ++j)
			f[i][j] = i + j;
	for( i = 0; i < n; ++i)
		for( j = 0; j < n; ++j)
		{
			for( k = 0; k < n; ++k)
			{
				if(j >= i)
				{
					g[i][j] = func(g[i][j], f[i][k], f[k][j]);
					g_useless[i][j] = func(g[i][j], f[i][k], f[k][j]);
					g_useless[i][j] = func(g[i][j], f[i][k], f[k][j]);
					g_useless[i][j] = func(g[i][j], f[i][k], f[k][j]);
				}
			}
		}
	int sum = 0;
	for( i = 0; i < n; ++i)
		for( j = 0; j < n; ++j)
			sum = sum + g[i][j];
	print(toString(sum));
	return 0;
}
