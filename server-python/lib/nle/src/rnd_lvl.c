/* NetHack 3.6	RND_lvl.c	$NHDT-Date: 1524689470 2018/04/25 20:51:10 $  $NHDT-Branch: NetHack-3.6.0 $:$NHDT-Revision: 1.18 $ */
/*      Copyright (c) 2004 by Robert Patrick Rankin               */
/* NetHack may be freely redistributed.  See license for details. */

#include "hack.h"

#ifdef USE_ISAAC64
#include "isaac64.h"

#if 0
static isaac64_ctx rng_state;
#endif

struct rnglist_t_lvl {
    int FDECL((*fn), (int));
    boolean init;
    isaac64_ctx rng_state;
};

enum { CORE = 0, DISP = 1 };

static struct rnglist_t_lvl rnglist_lvl[] = {
    { rn2_lvl, FALSE, { 0 } },                      /* CORE */
    { rn2_on_display_rng_lvl, FALSE, { 0 } },       /* DISP */
};

int
whichrng_lvl(fn)
int FDECL((*fn), (int));
{
    int i;

    for (i = 0; i < SIZE(rnglist_lvl); ++i)
        if (rnglist_lvl[i].fn == fn)
            return i;
    return -1;
}

void
init_isaac64_lvl(seed, fn)
unsigned long seed;
int FDECL((*fn), (int));
{
    unsigned char new_rng_state[sizeof seed];
    unsigned i;
    int rngindx = whichrng_lvl(fn);

    if (rngindx < 0)
        panic("Bad rng function passed to init_isaac64_lvl().");

    for (i = 0; i < sizeof seed; i++) {
        new_rng_state[i] = (unsigned char) (seed & 0xFF);
        seed >>= 8;
    }
    isaac64_init(&rnglist_lvl[rngindx].rng_state, new_rng_state,
                 (int) sizeof seed);
}

static int
RND_lvl(int x)
{
    return (isaac64_next_uint64(&rnglist_lvl[CORE].rng_state) % x);
}

/* 0 <= rn2_lvl(x) < x, but on a different sequence from the "main" rn2_lvl;
   used in cases where the answer doesn't affect gameplay and we don't
   want to give users easy control over the main RNG sequence. */
int
rn2_on_display_rng_lvl(x)
register int x;
{
    return (isaac64_next_uint64(&rnglist_lvl[DISP].rng_state) % x);
}

#else   /* USE_ISAAC64 */

/* "Rand()"s definition is determined by [OS]conf.h */
#if defined(LINT) && defined(UNIX) /* rand() is long... */
extern int NDECL(rand);
#define RND_lvl(x) (rand() % x)
#else /* LINT */
#if defined(UNIX) || defined(RANDOM)
#define RND_lvl(x) ((int) (Rand() % (long) (x)))
#else
/* Good luck: the bottom order bits are cyclic. */
#define RND_lvl(x) ((int) ((Rand() >> 3) % (x)))
#endif
#endif /* LINT */
int
rn2_on_display_rng_lvl(x)
register int x;
{
    static unsigned seed = 1;
    seed *= 2739110765;
    return (int)((seed >> 16) % (unsigned)x);
}
#endif  /* USE_ISAAC64 */

int
rn1_lvl(x, y)
register int x, y;
{
    return (rn2_lvl(x) + (y));
}

/* 0 <= rn2_lvl(x) < x */
int
rn2_lvl(x)
register int x;
{
#if (NH_DEVEL_STATUS != NH_STATUS_RELEASED)
    if (x <= 0) {
        impossible("rn2_lvl(%d_lvl) attempted", x);
        return 0;
    }
    x = RND_lvl(x);
    return x;
#else
    return RND_lvl(x);
#endif
}

/* 0 <= rnl_lvl(x) < x; sometimes subtracting Luck;
   good luck approaches 0, bad luck approaches (x-1) */
int
rnl_lvl(x)
register int x;
{
    register int i, adjustment;

#if (NH_DEVEL_STATUS != NH_STATUS_RELEASED)
    if (x <= 0) {
        impossible("rnl_lvl(%d_lvl) attempted", x);
        return 0;
    }
#endif

    adjustment = Luck;
    if (x <= 15) {
        /* for small ranges, use Luck/3 (rounded away from 0);
           also guard against architecture-specific differences
           of integer division involving negative values */
        adjustment = (abs(adjustment) + 1) / 3 * sgn(adjustment);
        /*
         *       11..13 ->  4
         *        8..10 ->  3
         *        5.. 7 ->  2
         *        2.. 4 ->  1
         *       -1,0,1 ->  0 (no adjustment)
         *       -4..-2 -> -1
         *       -7..-5 -> -2
         *      -10..-8 -> -3
         *      -13..-11-> -4
         */
    }

    i = RND_lvl(x);
    if (adjustment && rn2_lvl(37 + abs(adjustment))) {
        i -= adjustment;
        if (i < 0)
            i = 0;
        else if (i >= x)
            i = x - 1;
    }
    return i;
}

/* 1 <= RND_lvl(x) <= x */
int
rnd_lvl(x)
register int x;
{
#if (NH_DEVEL_STATUS != NH_STATUS_RELEASED)
    if (x <= 0) {
        impossible("rnd_lvl(%d_lvl) attempted", x);
        return 1;
    }
#endif
    x = RND_lvl(x) + 1;
    return x;
}

/* d_lvl(N,X) == NdX == dX+dX+...+dX N times; n <= d_lvl(n,x) <= (n*x) */
int
d_lvl(n, x)
register int n, x;
{
    register int tmp = n;

#if (NH_DEVEL_STATUS != NH_STATUS_RELEASED)
    if (x < 0 || n < 0 || (x == 0 && n != 0)) {
        impossible("d_lvl(%d,%d) attempted", n, x);
        return 1;
    }
#endif
    while (n--)
        tmp += RND_lvl(x);
    return tmp; /* Alea iacta est. -- J.C. */
}

/* 1 <= rne_lvl(x) <= max(u.ulevel/3,5) */
int
rne_lvl(x)
register int x;
{
    register int tmp, utmp;

    utmp = (u.ulevel < 15) ? 5 : u.ulevel / 3;
    tmp = 1;
    while (tmp < utmp && !rn2_lvl(x))
        tmp++;
    return tmp;

    /* was:
     *  tmp = 1;
     *  while (!rn2_lvl(x))
     *    tmp++;
     *  return min(tmp, (u.ulevel < 15) ? 5 : u.ulevel / 3);
     * which is clearer but less efficient and stands a vanishingly
     * small chance of overflowing tmp
     */
}

/* rnz_lvl: everyone's favorite! */
int
rnz_lvl(i)
int i;
{
#ifdef LINT
    int x = i;
    int tmp = 1000;
#else
    register long x = (long) i;
    register long tmp = 1000L;
#endif

    tmp += rn2_lvl(1000);
    tmp *= rne_lvl(4);
    if (rn2_lvl(2)) {
        x *= tmp;
        x /= 1000;
    } else {
        x *= 1000;
        x /= tmp;
    }
    return (int) x;
}

/*RND_lvl.c*/
