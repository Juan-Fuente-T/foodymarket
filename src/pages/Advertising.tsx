
import React from 'react';
import { Layout } from '@/components/layout/Layout';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { BadgePercent, TrendingUp, BarChart4, Users, DollarSign, Star } from 'lucide-react';
import { Link } from 'react-router-dom';

const Advertising = () => {
  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Advertise With Us</h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto">
            Boost your restaurant's visibility and reach more hungry customers with our advertising solutions.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-12">
          <Card>
            <CardHeader>
              <BadgePercent className="h-8 w-8 text-food-600 mb-2" />
              <CardTitle>Featured Listing</CardTitle>
              <CardDescription>
                Get prominent placement at the top of search results
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-gray-700 mb-4">
                Stand out from the competition with a premium listing that appears at the top of search results and category pages.
              </p>
              <ul className="space-y-2 text-gray-600">
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Top position in search results
                </li>
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Highlighted restaurant card
                </li>
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  "Featured" badge on your listing
                </li>
              </ul>
            </CardContent>
            <CardFooter>
              <Button asChild className="w-full bg-food-600 hover:bg-food-700">
                <Link to="/contact">Get Started</Link>
              </Button>
            </CardFooter>
          </Card>

          <Card>
            <CardHeader>
              <TrendingUp className="h-8 w-8 text-food-600 mb-2" />
              <CardTitle>Promotional Campaigns</CardTitle>
              <CardDescription>
                Targeted marketing to reach your ideal customers
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-gray-700 mb-4">
                Launch promotional campaigns that target specific customer segments based on location, order history, and preferences.
              </p>
              <ul className="space-y-2 text-gray-600">
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Targeted push notifications
                </li>
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Email marketing campaigns
                </li>
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Personalized discount offers
                </li>
              </ul>
            </CardContent>
            <CardFooter>
              <Button asChild className="w-full bg-food-600 hover:bg-food-700">
                <Link to="/contact">Learn More</Link>
              </Button>
            </CardFooter>
          </Card>

          <Card>
            <CardHeader>
              <BarChart4 className="h-8 w-8 text-food-600 mb-2" />
              <CardTitle>Performance Analytics</CardTitle>
              <CardDescription>
                Detailed insights on your advertising performance
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-gray-700 mb-4">
                Get comprehensive analytics on how your advertising campaigns are performing, with actionable insights.
              </p>
              <ul className="space-y-2 text-gray-600">
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Click-through rates
                </li>
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  Conversion tracking
                </li>
                <li className="flex items-center">
                  <div className="mr-2 h-4 w-4 rounded-full bg-green-500"></div>
                  ROI measurement
                </li>
              </ul>
            </CardContent>
            <CardFooter>
              <Button asChild className="w-full bg-food-600 hover:bg-food-700">
                <Link to="/contact">See Demo</Link>
              </Button>
            </CardFooter>
          </Card>
        </div>

        <div className="bg-gray-50 p-8 rounded-xl mb-12">
          <div className="text-center mb-8">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Why Advertise With Us?</h2>
            <p className="text-gray-600">Join thousands of successful restaurants that have boosted their business with our platform</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="flex flex-col items-center text-center">
              <Users className="h-10 w-10 text-food-600 mb-4" />
              <h3 className="text-lg font-medium mb-2">Large User Base</h3>
              <p className="text-gray-600">Reach over 1 million active food enthusiasts in your area</p>
            </div>

            <div className="flex flex-col items-center text-center">
              <DollarSign className="h-10 w-10 text-food-600 mb-4" />
              <h3 className="text-lg font-medium mb-2">Increase Revenue</h3>
              <p className="text-gray-600">Our partners see an average 35% increase in orders after advertising</p>
            </div>

            <div className="flex flex-col items-center text-center">
              <Star className="h-10 w-10 text-food-600 mb-4" />
              <h3 className="text-lg font-medium mb-2">Brand Recognition</h3>
              <p className="text-gray-600">Build your restaurant's reputation and create loyal customers</p>
            </div>
          </div>
        </div>

        <Card>
          <CardContent className="p-8">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-4">Ready to grow your restaurant business?</h2>
              <p className="text-gray-600 mb-6">
                Contact our advertising team today to create a customized strategy for your restaurant.
              </p>
              <Button asChild size="lg" className="bg-food-600 hover:bg-food-700">
                <Link to="/contact">Contact Sales Team</Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </Layout>
  );
};

export default Advertising;
